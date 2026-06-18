package org.tracker.ubus.ubus.TestDataLoader.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trips-data")
public class TripSampleDataController {

    private final TripRepository tripRepository;
    private final TripUserRepository tripUserRepository;
    private final TripHistoryRepository tripHistoryRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final UserRepository userRepository;

    @PostMapping("/save-new-trip-apk-apb")
    @Transactional
    public String saveNewTrip() {

        // 1. Get or create a BusAssignment for this trip
        BusAssignment busAssignment = busAssignmentRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No bus assignments found. Please create one first."));


        // 2. Create the Trip entity
        Trip trip = Trip.builder()
                .route(Route.APK_APB)
                .status(TripStatus.COMPLETE)
                .busAssignment(busAssignment)
                .totalCount(45)
                .departureTime(LocalDateTime.now().minusHours(2))
                .expectedArrivalTime(LocalDateTime.now().minusHours(1).minusMinutes(30))
                .build();

        Trip savedTrip = tripRepository.save(trip);

        // 3. Add 45 users to the trip (TripUser entities)
        List<User> availableUsers = userRepository.findByRoleInAndStatus(List.of(UserRole.STAFF, UserRole.STUDENT), UserStatus.ACTIVE);

        if (availableUsers.size() < 45) {
            throw new RuntimeException("Not enough users in database. Found: " + availableUsers.size());
        }

        // Take first 45 users (or create 45 if you need to)
        List<User> tripUsers = availableUsers.stream().limit(45).toList();

        List<TripUser> tripUserList = new ArrayList<>();
        for (User user : tripUsers) {
            TripUser tripUser = TripUser.builder()
                    .user(user)
                    .trip(savedTrip)
                    .build();
            tripUserList.add(tripUser);
        }

        tripUserRepository.saveAll(tripUserList);

        // 4. Add all route coordinates as TripHistoryPoint entities
        List<TripHistoryPoint> historyPoints = createHistoryPointsFromRoute(savedTrip);
        tripHistoryRepository.saveAll(historyPoints);

        return String.format("✅ Trip created successfully!\n" +
                        "Trip ID: %s\n" +
                        "Route: APK → APB\n" +
                        "Status: COMPLETE\n" +
                        "Total Users: %d\n" +
                        "History Points: %d\n" +
                        "Departure: %s\n" +
                        "Arrival: %s",
                savedTrip.getId(),
                tripUserList.size(),
                historyPoints.size(),
                savedTrip.getDepartureTime(),
                savedTrip.getExpectedArrivalTime());
    }

    private List<TripHistoryPoint> createHistoryPointsFromRoute(Trip trip) {
        // All 24 route coordinates from your OpenRouteService response
        // Format: [longitude, latitude]
        double[][] routeCoordinates = {
                {27.999301, -26.183109},
                {27.999394, -26.183247},
                {27.999434, -26.183391},
                {27.999492, -26.183428},
                {27.999524, -26.183399},
                {27.999569, -26.183399},
                {27.999602, -26.183428},
                {27.999602, -26.183468},
                {27.999578, -26.183494},
                {27.999397, -26.184264},
                {27.999562, -26.1843},
                {27.999736, -26.184338},
                {27.999919, -26.18438},
                {28.000089, -26.184416},
                {28.000521, -26.184506},
                {28.000576, -26.184539},
                {28.00059, -26.184575},
                {28.000584, -26.184671},
                {28.000495, -26.185009},
                {28.000503, -26.185093},
                {28.000411, -26.185261},
                {28.000377, -26.185278},
                {28.00034, -26.185284},
                {28.000112, -26.185233}
        };

        // Simulate varying speeds along the route (in km/h)
        double[] speeds = {
                35.2, 32.5, 30.1, 28.4, 29.3, 31.2, 33.8, 36.4, 38.1, 40.2,
                42.3, 44.1, 45.0, 44.8, 43.2, 41.5, 39.8, 37.4, 35.1, 33.2,
                31.4, 29.8, 28.1, 27.5
        };

        // Calculate time progression (trip took about 78 seconds total)
        // Distribute points over the duration
        LocalDateTime startTime = trip.getDepartureTime();
        LocalDateTime endTime = trip.getExpectedArrivalTime();
        long totalDurationSeconds = ChronoUnit.SECONDS.between(startTime, endTime);
        double secondsPerPoint = (double) totalDurationSeconds / routeCoordinates.length;

        List<TripHistoryPoint> historyPoints = new ArrayList<>();

        for (int i = 0; i < routeCoordinates.length; i++) {
            double lon = routeCoordinates[i][0];
            double lat = routeCoordinates[i][1];
            double speed = speeds[i % speeds.length]; // Cycle through speeds

            // Calculate timestamp for this point (evenly distributed along the route)
            LocalDateTime pointTime = startTime.plusSeconds((long)(i * secondsPerPoint));

            TripHistoryPoint point = TripHistoryPoint.builder()
                    .latitude(lat)
                    .longitude(lon)
                    .speed(speed)
                    .trip(trip)
                    .timePosted(pointTime)
                    .build();

            historyPoints.add(point);
        }

        return historyPoints;
    }
}