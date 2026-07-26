package org.tracker.ubus.ubus.Components.SIMULATION;

import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Handlers.DefaultRouteServiceCacheHandler;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Service.Interface.IBusLocationBatchService;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus.LOADING_PASSENGERS;
import static org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus.STATIONERY;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BusAndTripSimulationController {

    private final TripRepository tripRepository;
    private final IBusLocationBatchService busLocationBatchService;
    private final DefaultRouteServiceCacheHandler defaultRouteServiceCacheHandler;

    private static final Random random = new Random();
    private static final long SIMULATION_INTERVAL_MS = 1500;

    private final Cache<UUID, Trip> tripCache;
    private final Map<UUID, BusSimulationState> busStates = new ConcurrentHashMap<>();
    private final Set<UUID> loadingPassengersSent = ConcurrentHashMap.newKeySet();

    private boolean isInitialized = false;

    @PostConstruct
    protected void init() {
        if (isInitialized) {
            log.info("Simulation already running - skipping re-init");
            return;
        }
        isInitialized = true;

        LocalDate today = LocalDate.of(2026, 7, 24);

        var allTrips = this.tripRepository.findByStatus(TripStatus.IN_PROGRESS)
                .stream()
                .filter(trip -> isTripToday(trip, today))
                .filter(this::isWithinNoonAndMorning)
                .filter(trip -> !trip.getSchedule().isCompleted())
                .collect(Collectors.groupingBy(Trip::getRoute))
                .values()
                .stream()
                .map(List::getFirst)
                .toList();

        var tripCacheValues = allTrips.stream()
                .collect(Collectors.toMap(Trip::getId, trip -> trip));

        tripCacheValues.forEach((id, trip) -> {
            var bus = trip.getBusAssignment().getBus();
            log.info("Trip {} {} found for simulation", id, bus.getName());
        });
        this.tripCache.putAll(tripCacheValues);

        if (tripCache.estimatedSize() == 0) {
            log.warn("No trips found for today");
        } else {
            log.info("Initialized {} trips for simulation", tripCache.estimatedSize());

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
            if (this.tripCache.estimatedSize() == 0) {
                return;
            }

            var allTrips = this.tripCache.asMap()
                    .values()
                    .stream()
                    .toList();

            for (Trip trip : allTrips) {
                try {
                    UUID tripId = trip.getId();
                    var bus = trip.getBusAssignment().getBus();
                    var activityStatus = bus.getActivityStatus();

                    if (activityStatus == LOADING_PASSENGERS ||
                            activityStatus == STATIONERY) {
                        log.debug("Trip {} skipping simulation - bus is {}",
                                trip.getBusAssignment().getBus().getName(), activityStatus);
                        continue;
                    }

                    BusSimulationState state = busStates.computeIfAbsent(tripId,
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
        var schedule = trip.getSchedule();
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
                .currentDestIndex(-5) // -5 indicates UI mode (uses from/to from schedule)
                .build();

        busLocationBatchService.enqueue(locationMessage);
    }

    private void simulateBusMovement(Trip trip, BusSimulationState state) {
        var schedule = trip.getSchedule();
        var route = trip.getRoute();
        var busName = trip.getBusAssignment().getBus().getName();

        // Get all destinations in the route
        List<Destination> destinations = route.getDestinations();
        if (destinations == null || destinations.isEmpty()) {
            log.warn("No destinations found for route: {}", route.getLabel());
            return;
        }

        // Initialize - start at the 'from' destination
        if (!state.isInitialized) {
            Destination from = schedule.getFromDestination();
            int startIndex = destinations.indexOf(from);
            if (startIndex != -1) {
                state.currentDestIndex = startIndex;
            } else {
                state.currentDestIndex = 0;
            }
            state.isInitialized = true;
            log.info("Trip {} initialized at destination index {} ({})",
                    busName, state.currentDestIndex, destinations.get(state.currentDestIndex));
        }

        // Get current destination
        Destination currentDest = destinations.get(state.currentDestIndex);

        // Calculate next destination index (loop around)
        int nextDestIndex = (state.currentDestIndex + 1) % destinations.size();

        // Skip if same as current (shouldn't happen but just in case)
        while (destinations.get(nextDestIndex) == currentDest) {
            nextDestIndex = (nextDestIndex + 1) % destinations.size();
        }

        Destination nextDest = destinations.get(nextDestIndex);

        // Get coordinates for the segment from current to next destination
        List<LatLon> segmentCoordinates = this.defaultRouteServiceCacheHandler
                .getRouteSegmentCoordinates(route, currentDest, nextDest);

        if (segmentCoordinates == null || segmentCoordinates.isEmpty()) {
            log.warn("No coordinates found for segment from {} to {}", currentDest, nextDest);
            return;
        }

        String routeName = route.getLabel();
        double speed = random.nextDouble() * 10 + 20;

        // If we've reached the end of the segment, move to next destination
        if (state.currentIndex >= segmentCoordinates.size() - 1) {
            // Move to next destination
            state.currentDestIndex = nextDestIndex;
            state.currentIndex = 0;

            log.debug("Trip {} moved to next destination: {} (index {})",
                    busName, destinations.get(state.currentDestIndex), state.currentDestIndex);

            // Send location at the new destination
            Destination newDest = destinations.get(state.currentDestIndex);
            sendLocationUpdate(trip, newDest.getLat(), newDest.getLng(), 0, routeName, state.currentDestIndex);
            return;
        }

        // Move to next coordinate
        state.currentIndex++;
        LatLon nextPoint = segmentCoordinates.get(state.currentIndex);

        // Send location update with current destination index
        sendLocationUpdate(trip, nextPoint.lat(), nextPoint.lon(), speed, routeName, state.currentDestIndex);
    }

    private boolean isTripToday(Trip trip, LocalDate today) {
        LocalDateTime departureTime = trip.getDepartureTime();
        if (departureTime == null) {
            return false;
        }
        LocalDate tripDate = departureTime.toLocalDate();
        return tripDate.equals(today);
    }

    private void sendLocationUpdate(Trip trip, double lat, double lng, double speed, String route, int currentDestIndex) {
        var locationMessage = DriverCurrentLocationMessage.builder()
                .tripId(trip.getId())
                .latitude(lat)
                .longitude(lng)
                .speed(speed)
                .timePosted(LocalDateTime.now())
                .route(route)
                .busName(trip.getBusAssignment() != null && trip.getBusAssignment().getBus() != null
                        ? trip.getBusAssignment().getBus().getName()
                        : "Unknown")
                .currentDestIndex(currentDestIndex)
                .build();

        busLocationBatchService.enqueue(locationMessage);
    }

    private void cleanupCache(List<Trip> activeTrips) {
        List<UUID> activeTripIds = activeTrips.stream()
                .map(Trip::getId)
                .toList();

        busStates.keySet().removeIf(id -> !activeTripIds.contains(id));
    }

    private static class BusSimulationState {
        int currentIndex = 0;        // Current position within the segment coordinates
        int currentDestIndex = 0;    // Current destination index in the route
        boolean isInitialized = false;
    }

    private boolean isWithinHours(int hours, LocalDateTime time) {
        return time.isAfter(LocalDateTime.now().minusHours(hours));
    }


    private boolean isWithinNoonAndMorning(Trip trip) {
        var morning = LocalTime.of(10, 30);
        return trip.getDepartureTime()
                .toLocalTime()
                .isAfter(morning) &&
                trip.getDepartureTime()
                .toLocalTime()
                .isBefore(LocalTime.of(12, 0));
    }
 }