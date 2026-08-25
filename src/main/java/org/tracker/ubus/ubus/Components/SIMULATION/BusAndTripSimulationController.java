package org.tracker.ubus.ubus.Components.SIMULATION;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.Priority;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Repository.BusOperationalHistoryRepository;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Handlers.DefaultRouteServiceCacheHandler;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Service.Interface.IBusLocationTrackingService;
import org.tracker.ubus.ubus.Components.Trips.Trip.CacheManager.TripCacheManager;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus.LOADING_PASSENGERS;
import static org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus.STATIONERY;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusAndTripSimulationController {

    private final SimulatedTrips simulatedTrips;
    private final IBusLocationTrackingService busLocationBatchService;
    private final DefaultRouteServiceCacheHandler defaultRouteServiceCacheHandler;

    private static final Random random = new Random();
    private static final long SIMULATION_INTERVAL_MS = 800;


    private BusOperationalHistoryRepository busOperationalHistoryRepository;
    private final TripCacheManager tripCacheManager;
    private final Map<UUID, BusSimulationState> busStates = new ConcurrentHashMap<>();
    private final Set<UUID> loadingPassengersSent = ConcurrentHashMap.newKeySet();

    private boolean isInitialized = false;

    @PostConstruct
    protected void init() {
        if (isInitialized) {
            return;
        }
        isInitialized = true;

        var allTrips = this.simulatedTrips.getSimulationTrips();
        var tripCacheValues = allTrips.stream()
                .collect(Collectors.toMap(Trip::getId, trip -> trip));

        this.tripCacheManager.putAllInTripSimulationCache(tripCacheValues);

        if (this.tripCacheManager.estimatedTripSimulationCacheSize() == 0) {
            log.warn("No trips found for today");
        } else {
            log.info("Initialized {} trips for simulation", tripCacheManager.estimatedTripSimulationCacheSize());

            allTrips.forEach(trip -> {
                var bus = trip.getBusAssignment().getBus();

                if (bus.getActivityStatus() == LOADING_PASSENGERS ||
                        bus.getActivityStatus() == STATIONERY) {
                    sendLoadingPassengerLocation(trip);
                    loadingPassengersSent.add(trip.getId());
                }
            });
        }
    }

    @Scheduled(fixedRate = SIMULATION_INTERVAL_MS)
    protected void simulateTrips() {
        try {
            if (this.tripCacheManager.estimatedTotal() == 0)
                return;

            var allTrips = this.tripCacheManager.getAll();

            for (Trip trip : allTrips) {
                try {
                    UUID tripId = trip.getId();

                    var bus = trip.getBusAssignment().getBus();

                    var activityStatus = bus.getActivityStatus();
                    if (activityStatus == LOADING_PASSENGERS ||
                            activityStatus == STATIONERY) {
                        this.sendStationaryLocation(trip);
                        continue;
                    }

                    //var today = LocalDate.now();
//                    var operations = this.busOperationalHistoryRepository.findByBusAndPriorityAndDateOperated(bus,
//                            Priority.CRITICAL, today);


//                    for(var operation : operations)
//                        if(operation.getBus().equals(bus))
//                            this.sendStationaryLocation(trip);





                    var state = busStates.computeIfAbsent(tripId,
                            id -> new BusSimulationState());

                    simulateBusMovement(trip, state);

                } catch (Exception e) {
                    log.error("Failed to simulate trip: {}", trip.getId(), e);
                }
            }

            cleanupCache(allTrips);

        } catch (Exception e) {
            log.error("Error in simulation loop", e);
        }
    }

    private void sendLoadingPassengerLocation(Trip trip) {
        var schedule = trip.getScheduleLegBusAssignment()
                .getScheduleLeg();
        var from = schedule.getFromDestination();
        var route = trip.getRoute().getLabel();

        var locationMessage = DriverCurrentLocationMessage.builder()
                .tripId(trip.getId())
                .latitude(from.getLat())
                .longitude(from.getLng())
                .speed(0)
                .timePosted(LocalDateTime.now())
                .route(route)
                .busName(trip.getBusAssignment() != null && trip.getBusAssignment().getBus() != null
                        ? trip.getBusAssignment().getBus().getName()
                        : "Unknown")
                .isSimulated(trip.isFromSimulation())
                .build();

        busLocationBatchService.enqueue(locationMessage);
    }

    private void simulateBusMovement(Trip trip, BusSimulationState state) {
        var schedule = trip.getScheduleLegBusAssignment()
                .getScheduleLeg();
        var route = trip.getRoute();


        // Check if bus should be stopped
        if (shouldBusStop(state)) {
            sendLocationUpdate(trip, state.lastLat, state.lastLng, 0,
                    false);
            state.stoppedFrames++;
            return;
        }

        List<Destination> destinations = route.getUniqueStops();
        if (destinations.isEmpty()) {
            log.warn("No destinations found for route: {}", route.getLabel());
            return;
        }

        if (!state.isInitialized) {
            Destination from = schedule.getFromDestination();
            state.currentDestIndex = destinations.indexOf(from);

            if (state.currentDestIndex == -1)
                throw new IllegalStateException("From destination not found in route destinations");


            state.isInitialized = true;
            state.lastLat = from.getLat();
            state.lastLng = from.getLng();
        }

        var from = schedule.getFromDestination();
        var nextDest = schedule.getToDestination();

        // Get coordinates for the segment
        List<LatLon> segmentCoordinates = null;

        while (segmentCoordinates == null || segmentCoordinates.isEmpty()) {
            try {
                segmentCoordinates = this.defaultRouteServiceCacheHandler
                        .getRouteSegmentCoordinates(route, from, nextDest);
                if (segmentCoordinates != null && !segmentCoordinates.isEmpty()) {
                    break;
                }
            } catch (Exception e) {
                log.warn("No coordinates for {} -> {}, trying next", from, nextDest);
            }

            // Move to next destination
            int currentIndex = destinations.indexOf(nextDest);
            int nextIndex = (currentIndex + 1) % destinations.size();

            while (destinations.get(nextIndex).equals(nextDest)) {
                nextIndex = (nextIndex + 1) % destinations.size();
            }

            from = nextDest;
            nextDest = destinations.get(nextIndex);
            schedule.setFromDestination(from);
            schedule.setToDestination(nextDest);
        }

        double speed = random.nextDouble() * 10 + 20;


        if (state.currentIndex >= segmentCoordinates.size() - 1) {

            if(!trip.isFromSimulation())
                return;

            Destination currentDest = destinations.get(state.currentDestIndex);
            var nextDestToGo = this.getNextDestination(destinations, state.currentDestIndex);

            // Update schedule with new destinations
            schedule.setFromDestination(currentDest);
            schedule.setToDestination(nextDestToGo);

            state.currentDestIndex = destinations.indexOf(nextDestToGo);
            state.currentIndex = 0;
            state.stoppedFrames = 0;
            state.lastLat = currentDest.getLat();
            state.lastLng = currentDest.getLng();

            sendLocationUpdate(trip, currentDest.getLat(), currentDest.getLng(), 0, true);
            return;
        }

        // Move to next coordinate
        state.currentIndex++;
        LatLon nextPoint = segmentCoordinates.get(state.currentIndex);
        state.lastLat = nextPoint.lat();
        state.lastLng = nextPoint.lon();

        double actualSpeed = speed * (0.7 + random.nextDouble() * 0.6);
        sendLocationUpdate(trip, nextPoint.lat(), nextPoint.lon(), actualSpeed, false);
    }

    private boolean shouldBusStop(BusSimulationState state) {
        // If already stopped, check if it's time to move again
        if (state.isStopped) {
            state.stoppedFrames++;
            // Randomly decide to start moving after 2-10 frames
            if (state.stoppedFrames > random.nextInt(8) + 2) {
                state.isStopped = false;
                state.stoppedFrames = 0;
                return false;
            }
            return true;
        }

        // Random chance to stop (5% chance each frame)
        if (random.nextDouble() < 0.05) {
            state.isStopped = true;
            state.stoppedFrames = 0;
            return true;
        }

        return false;
    }

    private void sendLocationUpdate(Trip trip, double lat, double lng, double speed, boolean isMadeIt) {
        var locationBuilder = DriverCurrentLocationMessage.builder()
                .tripId(trip.getId())
                .latitude(lat)
                .longitude(lng)
                .speed(speed)
                .timePosted(LocalDateTime.now())
                .busName(trip.getBusAssignment() != null && trip.getBusAssignment().getBus() != null
                        ? trip.getBusAssignment().getBus().getName()
                        : "Unknown")
                .isSimulated(trip.isFromSimulation());

        if (trip.isFromSimulation())
            locationBuilder
                    .isMadeIt(isMadeIt);


        var locationMessage = locationBuilder.build();
        busLocationBatchService.enqueue(locationMessage);
    }

    private void cleanupCache(Collection<Trip> activeTrips) {
        List<UUID> activeTripIds = activeTrips.stream()
                .map(Trip::getId)
                .toList();

        busStates.keySet()
                .removeIf(id -> !activeTripIds.contains(id));
    }

    private static class BusSimulationState {
        int currentIndex = 0;
        int currentDestIndex = 0;
        boolean isInitialized = false;
        boolean isStopped = false;
        int stoppedFrames = 0;
        double lastLat = 0;
        double lastLng = 0;
    }


    private Destination getNextDestination(List<Destination> destinations, int currentIndex) {

        if (currentIndex >= destinations.size() - 1)
            return destinations.getFirst();
        else
            return destinations.get(currentIndex + 1);
    }


    private void sendStationaryLocation(Trip trip) {
        var schedule = trip.getScheduleLegBusAssignment()
                .getScheduleLeg();
        var from = schedule.getFromDestination();
        var route = trip.getRoute().getLabel();
        var busName = trip.getBusAssignment() != null && trip.getBusAssignment().getBus() != null
                ? trip.getBusAssignment().getBus().getName()
                : "Unknown";

        var locationMessage = DriverCurrentLocationMessage.builder()
                .tripId(trip.getId())
                .latitude(from.getLat())
                .longitude(from.getLng())
                .speed(0)
                .timePosted(LocalDateTime.now())
                .route(route)
                .busName(busName)
                .isSimulated(trip.isFromSimulation())
                .build();

        busLocationBatchService.enqueue(locationMessage);
    }
}