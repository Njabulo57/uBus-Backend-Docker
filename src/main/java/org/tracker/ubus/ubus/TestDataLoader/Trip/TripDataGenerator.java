package org.tracker.ubus.ubus.TestDataLoader.Trip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trip.Enum.TripRoute;
import org.tracker.ubus.ubus.Components.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trip.Repository.TripRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Order(4)
@Slf4j
public class TripDataGenerator implements CommandLineRunner {

    private final TripRepository tripRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final Random random = new Random();

    private static final int NUMBER_OF_TRIPS = 10;
    private static final int REPORTS_PER_TRIP = 100; // For future tracking simulation

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== STARTING TRIP DATA GENERATION ===");

        // Check if we already have trips
        if (tripRepository.count() > 0) {
            log.info("Trips already exist in database. Skipping generation.");
            return;
        }

        // Get all bus assignments (need drivers and buses assigned)
        List<BusAssignment> busAssignments = busAssignmentRepository.findAll();

        if (busAssignments.isEmpty()) {
            log.warn("No bus assignments found! Please generate BusAssignments first.");
            log.warn("Skipping Trip generation until bus assignments exist.");
            return;
        }

        log.info("Found {} bus assignments for trip generation", busAssignments.size());

        // Generate trips
        List<Trip> trips = generateTrips(busAssignments);

        // Save to database
        List<Trip> savedTrips = tripRepository.saveAll(trips);

        log.info("✅ Successfully generated {} IN_PROGRESS trips", savedTrips.size());
        log.info("=== TRIP DATA GENERATION COMPLETE ===");

        // Print summary
        printTripSummary(savedTrips);
    }

    private List<Trip> generateTrips(List<BusAssignment> busAssignments) {
        List<Trip> trips = new ArrayList<>();
        TripRoute[] allRoutes = TripRoute.values();

        // Ensure we don't try to create more trips than available bus assignments
        int tripsToCreate = Math.min(NUMBER_OF_TRIPS, busAssignments.size());

        log.info("Creating {} trips from {} available bus assignments", tripsToCreate, busAssignments.size());

        for (int i = 0; i < tripsToCreate; i++) {
            BusAssignment busAssignment = busAssignments.get(i);
            TripRoute randomRoute = allRoutes[random.nextInt(allRoutes.length)];

            // Random total count between 30-100 (simulating passenger count or report count)
            int totalCount = 30 + random.nextInt(71);

            Trip trip = Trip.builder()

                    .route(randomRoute)
                    .status(TripStatus.IN_PROGRESS)  // All IN_PROGRESS as requested
                    .busAssignment(busAssignment)
                    .totalCount(totalCount)
                    .build();

            trips.add(trip);

            log.debug("Generated trip: Bus {} - Route: {} - Status: {} - Count: {}",
                    busAssignment.getBus().getName(),
                    randomRoute.getLabel(),
                    TripStatus.IN_PROGRESS.getLabel(),
                    totalCount);
        }

        return trips;
    }

    private void printTripSummary(List<Trip> trips) {
        log.info("\n📊 TRIP GENERATION SUMMARY:");
        log.info("┌─────────────────────────────────────────────────────────────┐");
        log.info(String.format("│ Total Trips Generated:     %-30d│", trips.size()));
        log.info(String.format("│ Status:                    %-30s│", "ALL IN_PROGRESS"));
        log.info("├─────────────────────────────────────────────────────────────┤");
        log.info("│ TRIP DETAILS:                                              │");

        for (int i = 0; i < Math.min(trips.size(), 10); i++) {
            Trip trip = trips.get(i);
            log.info(String.format("│ %d. Bus: %-20s Route: %-25s│",
                    i + 1,
                    trip.getBusAssignment().getBus().getName(),
                    trip.getRoute().getLabel()));
        }

        if (trips.size() > 10) {
            log.info(String.format("│ ... and %d more trips                                      │", trips.size() - 10));
        }

        log.info("└─────────────────────────────────────────────────────────────┘");

        // Route distribution
        log.info("\n📈 ROUTE DISTRIBUTION:");
        java.util.Map<TripRoute, Long> routeCount = trips.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Trip::getRoute,
                        java.util.stream.Collectors.counting()
                ));

        routeCount.forEach((route, count) ->
                log.info(String.format("   %-20s: %d trips", route.getLabel(), count))
        );
    }

    // Helper method to generate trips on demand (not just at startup)
    @Transactional
    public List<Trip> generateAdditionalTrips(int numberOfTrips) {
        List<BusAssignment> busAssignments = busAssignmentRepository.findAll();

        if (busAssignments.isEmpty()) {
            log.error("Cannot generate trips: No bus assignments available");
            return List.of();
        }

        List<Trip> newTrips = new ArrayList<>();
        TripRoute[] allRoutes = TripRoute.values();

        for (int i = 0; i < numberOfTrips; i++) {
            BusAssignment randomAssignment = busAssignments.get(random.nextInt(busAssignments.size()));
            TripRoute randomRoute = allRoutes[random.nextInt(allRoutes.length)];
            int totalCount = 30 + random.nextInt(71);

            Trip trip = Trip.builder()
                    .id(UUID.randomUUID())
                    .route(randomRoute)
                    .status(TripStatus.IN_PROGRESS)
                    .busAssignment(randomAssignment)
                    .totalCount(totalCount)
                    .build();

            newTrips.add(trip);
        }

        List<Trip> savedTrips = tripRepository.saveAll(newTrips);
        log.info("✅ Generated {} additional IN_PROGRESS trips", savedTrips.size());

        return savedTrips;
    }

    // Alternative generator with specific route
    @Transactional
    public Trip createTripForBusAssignment(UUID busAssignmentId, TripRoute route, int totalCount) {
        BusAssignment busAssignment = busAssignmentRepository.findById(busAssignmentId)
                .orElseThrow(() -> new RuntimeException("BusAssignment not found with id: " + busAssignmentId));

        Trip trip = Trip.builder()
                .id(UUID.randomUUID())
                .route(route)
                .status(TripStatus.IN_PROGRESS)
                .busAssignment(busAssignment)
                .totalCount(totalCount)
                .build();

        Trip savedTrip = tripRepository.save(trip);
        log.info("✅ Created trip: Bus {} - Route: {} - Total Count: {}",
                busAssignment.getBus().getName(),
                route.getLabel(),
                totalCount);

        return savedTrip;
    }
}