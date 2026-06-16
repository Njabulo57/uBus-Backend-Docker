package org.tracker.ubus.ubus.TestDataLoader.Trip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.Entity.TripHistoryPoint;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.Repository.TripHistoryRepository;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Repository.TripUserRepository;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Campus;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class EverythingTripDataGenerator {

    private final UserRepository userRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final TripRepository tripRepository;
    private final TripHistoryRepository tripHistoryRepository;
    private final TripUserRepository tripUserRepository;
    private final PlatformTransactionManager transactionManager;

    private static final Map<Campus, double[]> CAMPUS_COORDS = Map.of(
            Campus.SOWETO,       new double[]{-26.2612,    27.92155},
            Campus.DOORNFONTEIN, new double[]{-26.19275,   28.05427},
            Campus.APK,          new double[]{-26.18441,   27.99607},
            Campus.APB,          new double[]{-26.1904167, 28.0193056}
    );

    // Optimized values for speed
    private static final double SPEED_MPS       = 8.33;
    private static final int    POINTS_PER_TRIP = 50;
    private static final int    TOTAL_TRIPS     = 200;      // Total trips to create
    private static final int    MIN_PASSENGERS  = 10;       // Minimum passengers per trip
    private static final int    MAX_PASSENGERS  = 40;       // Maximum passengers per trip
    private static final int    BATCH_SIZE      = 50;

    private static final Map<Route, double[][]> ROUTE_COORDS = new HashMap<>();

    static {
        for (Route route : Route.values()) {
            double[] from = CAMPUS_COORDS.get(route.getFromCampus());
            double[] to = CAMPUS_COORDS.get(route.getToCampus());
            if (from != null && to != null) {
                ROUTE_COORDS.put(route, new double[][]{from, to});
            }
        }
    }

    public void generateCompletedTrips() {
        long startTime = System.currentTimeMillis();
        log.info("=== Starting Trip Data Generation ===");
        log.info("Config: totalTrips={}, passengersPerTrip={}-{}, pointsPerTrip={}",
                TOTAL_TRIPS, MIN_PASSENGERS, MAX_PASSENGERS, POINTS_PER_TRIP);

        // Get all potential passengers
        List<User> allPassengers = userRepository.findByRoleIn(List.of(UserRole.STUDENT, UserRole.STAFF));
        if (allPassengers.isEmpty()) {
            log.warn("No students or staff found.");
            return;
        }
        log.info("Found {} potential passengers", allPassengers.size());

        // Get bus assignments
        List<BusAssignment> busAssignments = busAssignmentRepository.findAll();
        if (busAssignments.isEmpty()) {
            log.error("No bus assignments found.");
            return;
        }
        log.info("Found {} bus assignments", busAssignments.size());

        // Data containers
        List<Trip> allTrips = new ArrayList<>();
        List<TripUser> allTripUsers = new ArrayList<>();
        List<TripHistoryPoint> allPoints = new ArrayList<>();

        Random random = new Random(42);

        int totalTripUsersCreated = 0;

        for (int tripNum = 0; tripNum < TOTAL_TRIPS; tripNum++) {
            // Select random route and bus
            Route route = Route.values()[random.nextInt(Route.values().length)];
            BusAssignment bus = busAssignments.get(random.nextInt(busAssignments.size()));

            // Determine how many passengers for this trip
            int passengerCount = MIN_PASSENGERS + random.nextInt(MAX_PASSENGERS - MIN_PASSENGERS + 1);

            // Create trip with totalCount set to passengerCount
            Trip trip = Trip.builder()
                    .route(route)
                    .status(TripStatus.COMPLETE)
                    .busAssignment(bus)
                    .totalCount(passengerCount)  // This now matches actual passenger count
                    .departureTime(LocalDateTime.now()
                            .minusDays(random.nextInt(30))
                            .minusHours(random.nextInt(24)))
                    .build();
            allTrips.add(trip);

            // Select random passengers for this trip (no duplicates on same trip)
            Set<User> selectedPassengers = new HashSet<>();
            List<User> availablePassengers = new ArrayList<>(allPassengers);
            Collections.shuffle(availablePassengers, random);

            for (int i = 0; i < passengerCount && i < availablePassengers.size(); i++) {
                User passenger = availablePassengers.get(i);
                selectedPassengers.add(passenger);

                TripUser tripUser = TripUser.builder()
                        .user(passenger)
                        .trip(trip)
                        .build();
                allTripUsers.add(tripUser);
            }

            // If not enough passengers, use duplicates (but this shouldn't happen with enough users)
            if (selectedPassengers.size() < passengerCount) {
                log.warn("Not enough unique passengers for trip {}. Needed {} but had {}",
                        tripNum, passengerCount, selectedPassengers.size());
                // Fill remaining with random passengers (allow duplicates on same trip? No, skip)
                // Just use what we have and update totalCount
                trip.setTotalCount(selectedPassengers.size());
            }

            totalTripUsersCreated += selectedPassengers.size();

            // Generate GPS points for this trip
            generatePointsFast(trip, route, random, allPoints);

            // Log progress
            if ((tripNum + 1) % 50 == 0) {
                log.info("Generated {}/{} trips, {} passenger links created so far",
                        tripNum + 1, TOTAL_TRIPS, totalTripUsersCreated);
            }
        }

        log.info("Generated {} trips, {} passenger links, {} GPS points in memory",
                allTrips.size(), allTripUsers.size(), allPoints.size());

        // Save everything in a transaction
        long saveStart = System.currentTimeMillis();
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.execute(status -> {
            log.info("Saving to database...");

            // Save trips first
            List<Trip> savedTrips = tripRepository.saveAll(allTrips);
            log.info("Saved {} trips", savedTrips.size());

            // Link relationships to managed trips
            for (int i = 0; i < savedTrips.size(); i++) {
                Trip managedTrip = savedTrips.get(i);
                Trip originalTrip = allTrips.get(i);

                // Update TripUser references
                for (TripUser tu : allTripUsers) {
                    if (tu.getTrip() == originalTrip) {
                        tu.setTrip(managedTrip);
                    }
                }

                // Update TripHistoryPoint references
                for (TripHistoryPoint p : allPoints) {
                    if (p.getTrip() == originalTrip) {
                        p.setTrip(managedTrip);
                    }
                }
            }

            // Save relationships
            tripUserRepository.saveAll(allTripUsers);
            log.info("Saved {} passenger-trip links", allTripUsers.size());

            tripHistoryRepository.saveAll(allPoints);
            log.info("Saved {} GPS points", allPoints.size());

            return null;
        });

        long totalTime = System.currentTimeMillis() - startTime;
        long saveTime = System.currentTimeMillis() - saveStart;

        log.info("=== DONE in {} ms (save took {} ms) ===", totalTime, saveTime);
        log.info("Summary:");
        log.info("  - {} trips created", allTrips.size());
        log.info("  - {} passenger links created (avg {:.1f} per trip)",
                allTripUsers.size(), (double)allTripUsers.size() / allTrips.size());
        log.info("  - {} GPS points created", allPoints.size());
    }

    private void generatePointsFast(Trip trip, Route route, Random random, List<TripHistoryPoint> allPoints) {
        double[][] coords = ROUTE_COORDS.get(route);
        if (coords == null) return;

        double fromLat = coords[0][0];
        double fromLon = coords[0][1];
        double toLat = coords[1][0];
        double toLon = coords[1][1];

        LocalDateTime currentTime = trip.getDepartureTime();

        for (int i = 0; i <= POINTS_PER_TRIP; i++) {
            double fraction = (double) i / POINTS_PER_TRIP;

            // Linear interpolation
            double lat = fromLat + (toLat - fromLat) * fraction;
            double lon = fromLon + (toLon - fromLon) * fraction;

            // Add GPS noise
            lat += (random.nextDouble() - 0.5) * 0.001;
            lon += (random.nextDouble() - 0.5) * 0.001;

            // Vary speed along route
            double speed = SPEED_MPS + (random.nextDouble() - 0.5) * 3.0;

            TripHistoryPoint point = TripHistoryPoint.builder()
                    .latitude(lat)
                    .longitude(lon)
                    .speed(Math.max(0, speed))
                    .trip(trip)
                    .timePosted(currentTime)
                    .build();
            allPoints.add(point);

            // 30-60 seconds between points
            currentTime = currentTime.plusSeconds(30 + random.nextInt(31));
        }
    }
}