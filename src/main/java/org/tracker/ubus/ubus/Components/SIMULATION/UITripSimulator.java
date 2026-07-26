package org.tracker.ubus.ubus.Components.SIMULATION;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Handlers.DefaultRouteServiceCacheHandler;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Service.Impl.BusLocationBatchService;
import org.tracker.ubus.ubus.Components.Trips.Trip.CacheManager.TripCacheManager;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus.LOADING_PASSENGERS;

@Slf4j
@Component
@RequiredArgsConstructor
public class UITripSimulator {

    private final BusLocationBatchService busLocationBatchService;
    private final DefaultRouteServiceCacheHandler defaultRouteServiceCacheHandler;
    private final TripCacheManager tripCacheManager;

    private static final Random random = new Random();
    private final Map<UUID, BusSimulationState> busStates = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 1000)
    protected void simulateTripMovement() {
        // Get all trips from demonstration cache
        var tripMap = tripCacheManager.getAllFromTripDemonstrationCacheAsMap();
        if (tripMap.isEmpty()) {
            log.debug("No trips in demonstration cache to simulate");
            return;
        }

        for (var entry : tripMap.entrySet()) {
            UUID tripId = entry.getKey();
            var tripDestinationMap = entry.getValue();

            // Get the trip and its destinations
            Trip trip = tripDestinationMap.keySet()
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (trip == null) {
                log.warn("No trip found for ID: {}", tripId);
                continue;
            }

            var destinations = tripDestinationMap.get(trip);
            if (destinations == null || destinations.isEmpty()) {
                log.warn("No destinations found for trip: {}", tripId);
                continue;
            }

            // Check if bus is loading passengers - skip if it is (don't move)
            var bus = trip.getBusAssignment().getBus();
            if (bus.getActivityStatus() == LOADING_PASSENGERS) {
                log.debug("Trip {} NOT moving - bus is loading passengers at {}",
                        trip.getBusAssignment().getBus().getName(),
                        trip.getSchedule().getFromDestination());
                continue; // Skip this trip entirely, don't move
            }

            try {
                BusSimulationState state = busStates.computeIfAbsent(tripId,
                        id -> new BusSimulationState());

                var to = destinations.getLast();
                simulateTripToDestination(trip, to, state);
            } catch (Exception e) {
                log.error("Failed to simulate trip: {}", trip.getId(), e);
            }
        }

        cleanupCache(tripMap);
    }

    private void simulateTripToDestination(Trip trip, Destination destination, BusSimulationState state) {
        var route = trip.getRoute();
        var from = trip.getSchedule().getFromDestination();

        // Use the handler to get segment coordinates
        List<LatLon> segmentCoordinates = defaultRouteServiceCacheHandler
                .getRouteSegmentCoordinates(route, from, destination);

        if (segmentCoordinates == null || segmentCoordinates.isEmpty()) {
            log.warn("No coordinates found for segment from {} to {}", from, destination);
            return;
        }

        // Initialize
        if (!state.isInitialized) {
            state.currentIndex = 0;
            state.isInitialized = true;
            log.info("Trip {} starting from {} to {}",
                    trip.getBusAssignment().getBus().getName(), from, destination);
        }

        // Check if reached destination
        if (state.currentIndex >= segmentCoordinates.size() - 1) {
            log.info("Trip {} reached destination: {}",
                    trip.getBusAssignment().getBus().getName(), destination);
            busStates.remove(trip.getId());
            // Remove from demonstration cache when reached
            tripCacheManager.removeFromTripDemonstrationCache(trip);
            return;
        }

        // Move to next coordinate
        state.currentIndex++;
        LatLon nextPoint = segmentCoordinates.get(state.currentIndex);
        double speed = random.nextDouble() * 10 + 20;

        sendLocationUpdate(trip, nextPoint.lat(), nextPoint.lon(), speed);
    }

    private void sendLocationUpdate(Trip trip, double lat, double lng, double speed) {
        var locationMessage = DriverCurrentLocationMessage.builder()
                .tripId(trip.getId())
                .latitude(lat)
                .longitude(lng)
                .speed(speed)
                .currentDestIndex(-5)
                .timePosted(LocalDateTime.now())
                .route(trip.getRoute().getLabel())
                .busName(trip.getBusAssignment() != null && trip.getBusAssignment().getBus() != null
                        ? trip.getBusAssignment().getBus().getName()
                        : "Unknown")
                .build();

        busLocationBatchService.enqueue(locationMessage);
    }

    private void cleanupCache(Map<UUID, Map<Trip, SequencedCollection<Destination>>> tripMap) {
        // Remove states for trips that are no longer in the demonstration cache
        busStates.keySet().removeIf(id ->
                !tripMap.containsKey(id)
        );
    }

    private static class BusSimulationState {
        int currentIndex = 0;
        boolean isInitialized = false;
    }
}