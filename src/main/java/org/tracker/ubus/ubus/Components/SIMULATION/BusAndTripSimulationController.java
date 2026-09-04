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
    private static final long SIMULATION_INTERVAL_MS = 1_500;

    private BusOperationalHistoryRepository busOperationalHistoryRepository;
    private final TripCacheManager tripCacheManager;
    private final Map<UUID, BusSimulationState> busStates = new ConcurrentHashMap<>();
    private final Set<UUID> loadingPassengersSent = ConcurrentHashMap.newKeySet();


    @PostConstruct
    protected void init() {


//        var allTrips = this.simulatedTrips.getUniqueTrips();
//
//        System.err.println(allTrips.size() + " trips found for simulation");
//        var tripCacheValues = allTrips.stream()
//                .collect(Collectors.toMap(Trip::getId, trip -> trip));
//
//        this.tripCacheManager.putAllInTripSimulationCache(tripCacheValues);
//
//        if (this.tripCacheManager.estimatedTripSimulationCacheSize() == 0)
//            log.warn("No trips found for today");

    }

    @Scheduled(fixedRate = SIMULATION_INTERVAL_MS)
    protected void simulateTrips() {
        try {
            if (this.tripCacheManager.estimatedTotal() == 0)
                return;

            var allTrips = this.tripCacheManager.getAll();

            for (var trip : allTrips) {
                try {
                    var tripId = trip.getId();

                    var bus = trip.getBusAssignment().getBus();

                    var activityStatus = bus.getActivityStatus();
                    if (activityStatus == LOADING_PASSENGERS ||
                            activityStatus == STATIONERY) {
                        this.sendStationaryLocation(trip);
                        continue;
                    }

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


    private void simulateBusMovement(Trip trip, BusSimulationState state) {
        var schedule = trip.getScheduleLegBusAssignment()
                .getScheduleLeg();
        var route = trip.getRoute();

        // Check if bus should be stopped
        if (shouldBusStop(state)) {
            sendLocationUpdate(trip, state.lastLat, state.lastLng, 0, false);
            state.stoppedFrames++;
            return;
        }

        List<Destination> destinations = route.getDestinations();
        if (destinations.isEmpty()) {
            log.warn("No destinations found for route: {}", route.getLabel());
            return;
        }

        // Initialize state if needed
        if (!state.isInitialized) {
            Destination from = schedule.getFromDestination();
            state.currentDestIndex = destinations.indexOf(from);

            if (state.currentDestIndex == -1) {
                for (int i = 0; i < destinations.size(); i++) {
                    if (destinations.get(i).equals(from)) {
                        state.currentDestIndex = i;
                        break;
                    }
                }
                if (state.currentDestIndex == -1) {
                    throw new IllegalStateException("From destination not found in route destinations");
                }
            }

            state.isInitialized = true;
            state.lastLat = from.getLat();
            state.lastLng = from.getLng();
        }

        // Get current and next destinations based on current index
        Destination from = destinations.get(state.currentDestIndex);
        Destination nextDest = destinations.get((state.currentDestIndex + 1) % destinations.size());

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
            int currentIndex = state.currentDestIndex;
            int nextIndex = (currentIndex + 1) % destinations.size();

            while (destinations.get(nextIndex).equals(destinations.get(currentIndex)))
                nextIndex = (nextIndex + 1) % destinations.size();

            from = destinations.get(currentIndex);
            nextDest = destinations.get(nextIndex);
            state.currentDestIndex = nextIndex;

            // ✅ ONLY UPDATE SCHEDULE FOR SIMULATED TRIPS
            if (trip.isFromSimulation()) {
                schedule.setFromDestination(from);
                schedule.setToDestination(nextDest);
            }
        }

        // Speed calculation: mostly 12-18 km/h with occasional spikes
        double speed;
        double randomFactor = random.nextDouble();

        if (randomFactor < 0.7) {
            speed = 12 + random.nextDouble() * 6;
        } else if (randomFactor < 0.9) {
            speed = 18 + random.nextDouble() * 7;
        } else if (randomFactor < 0.97) {
            speed = 25 + random.nextDouble() * 10;
        } else {
            speed = 35 + random.nextDouble() * 15;
        }

        speed = speed * (0.9 + random.nextDouble() * 0.2);

        var currentCoIndx = state.currentIndex;
        var legDestIndx = segmentCoordinates.size() - 1;

        if (SimulationUtil.isAtEndOfLeg(currentCoIndx, legDestIndx)) {

            if(!trip.isFromSimulation())
                return;

            int nextDestIndex = (state.currentDestIndex + 1) % destinations.size();
            Destination currentDest = destinations.get(state.currentDestIndex);
            Destination nextDestToGo = destinations.get(nextDestIndex);


            if (trip.isFromSimulation()) {
                schedule.setFromDestination(currentDest);
                schedule.setToDestination(nextDestToGo);
            }

            state.currentDestIndex = nextDestIndex;
            state.currentIndex = 0;
            state.stoppedFrames = 0;
            state.lastLat = currentDest.getLat();
            state.lastLng = currentDest.getLng();

            sendLocationUpdate(trip, currentDest.getLat(), currentDest.getLng(), 0, true);
            return;
        }

        int stepsToMove = 3;
        int newIndex = Math.min(state.currentIndex + stepsToMove, segmentCoordinates.size() - 1);
        state.currentIndex = newIndex;

        LatLon nextPoint = segmentCoordinates.get(state.currentIndex);
        state.lastLat = nextPoint.lat();
        state.lastLng = nextPoint.lon();

        sendLocationUpdate(trip, nextPoint.lat(), nextPoint.lon(), speed, false);
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
            locationBuilder.isMadeIt(isMadeIt);

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