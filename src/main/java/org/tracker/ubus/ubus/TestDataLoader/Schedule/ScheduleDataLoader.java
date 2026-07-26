package org.tracker.ubus.ubus.TestDataLoader.Schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Enum.DriverSchedule;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Enum.ScheduleTripStatus;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleRepository;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(4)
public class ScheduleDataLoader implements CommandLineRunner {

    private final ScheduleRepository scheduleRepository;
    private final BusRepository busRepository;
    private final BusAssignmentRepository busAssignmentRepository;

    private static final LocalDate START_DATE = LocalDate.of(2026, 3, 16);
    private static final LocalDate END_DATE = LocalDate.of(2026, 11, 30);

    private static final Set<LocalDate> RECESS_DATES = Set.of(
            LocalDate.of(2026, 3, 28),
            LocalDate.of(2026, 4, 4),
            LocalDate.of(2026, 5, 23),
            LocalDate.of(2026, 5, 24),
            LocalDate.of(2026, 5, 25),
            LocalDate.of(2026, 5, 26),
            LocalDate.of(2026, 5, 27),
            LocalDate.of(2026, 5, 30),
            LocalDate.of(2026, 5, 31),
            LocalDate.of(2026, 6, 20),
            LocalDate.of(2026, 6, 21),
            LocalDate.of(2026, 6, 22),
            LocalDate.of(2026, 6, 23),
            LocalDate.of(2026, 6, 24),
            LocalDate.of(2026, 6, 25),
            LocalDate.of(2026, 6, 26),
            LocalDate.of(2026, 6, 27),
            LocalDate.of(2026, 6, 28),
            LocalDate.of(2026, 6, 29),
            LocalDate.of(2026, 6, 30),
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 2),
            LocalDate.of(2026, 7, 3),
            LocalDate.of(2026, 7, 4),
            LocalDate.of(2026, 7, 5),
            LocalDate.of(2026, 8, 29),
            LocalDate.of(2026, 8, 30),
            LocalDate.of(2026, 8, 31),
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2026, 9, 2),
            LocalDate.of(2026, 9, 3),
            LocalDate.of(2026, 9, 4),
            LocalDate.of(2026, 9, 5),
            LocalDate.of(2026, 9, 6),
            LocalDate.of(2026, 10, 17),
            LocalDate.of(2026, 10, 18),
            LocalDate.of(2026, 10, 19),
            LocalDate.of(2026, 10, 20),
            LocalDate.of(2026, 10, 21),
            LocalDate.of(2026, 11, 16),
            LocalDate.of(2026, 11, 17),
            LocalDate.of(2026, 11, 18),
            LocalDate.of(2026, 11, 19),
            LocalDate.of(2026, 11, 20),
            LocalDate.of(2026, 11, 21),
            LocalDate.of(2026, 11, 22)
    );

    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 3, 21),
            LocalDate.of(2026, 4, 6),
            LocalDate.of(2026, 4, 27),
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 6, 16),
            LocalDate.of(2026, 8, 10),
            LocalDate.of(2026, 9, 24),
            LocalDate.of(2026, 12, 16)
    );

    private static final Map<Route, List<Destination>> ROUTE_PATHS_FORWARD = Map.ofEntries(
            Map.entry(Route.ROUTE_1, Arrays.asList(Destination.DFC, Destination.APB, Destination.APK)),
            Map.entry(Route.ROUTE_2, Arrays.asList(Destination.SWC, Destination.APK, Destination.APB)),
            Map.entry(Route.ROUTE_3, Arrays.asList(Destination.SWC, Destination.DFC)),
            Map.entry(Route.ROUTE_JBS, Arrays.asList(Destination.APK, Destination.APB, Destination.JBS))
    );

    private static final Map<Route, List<Destination>> ROUTE_PATHS_REVERSE = Map.ofEntries(
            Map.entry(Route.ROUTE_1, Arrays.asList(Destination.APK, Destination.APB, Destination.DFC)),
            Map.entry(Route.ROUTE_2, Arrays.asList(Destination.APB, Destination.APK, Destination.SWC)),
            Map.entry(Route.ROUTE_3, Arrays.asList(Destination.DFC, Destination.SWC)),
            Map.entry(Route.ROUTE_JBS, Arrays.asList(Destination.JBS, Destination.APB, Destination.APK))
    );

    private static final Map<String, Integer> TRAVEL_TIMES = Map.ofEntries(
            Map.entry("DFC-APB", 30),
            Map.entry("APB-DFC", 30),
            Map.entry("APB-APK", 10),
            Map.entry("APK-APB", 10),
            Map.entry("SWC-APK", 40),
            Map.entry("APK-SWC", 40),
            Map.entry("SWC-DFC", 45),
            Map.entry("DFC-SWC", 45),
            Map.entry("APB-JBS", 10),
            Map.entry("JBS-APB", 10),
            Map.entry("APK-JBS", 20),
            Map.entry("JBS-APK", 20)
    );

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Starting Schedule Data Loader ===");

        if (scheduleRepository.count() > 0) {
            log.info("Schedules already exist. Skipping load...");
            return;
        }

        if (busAssignmentRepository.count() == 0) {
            log.error("No bus assignments found. Please run BusAssignmentTestDataGenerator first.");
            return;
        }

        List<BusAssignment> assignments = busAssignmentRepository.findAll();
        log.info("Found {} bus assignments", assignments.size());

        List<Schedule> allSchedules = new ArrayList<>();

        Map<Bus, List<BusAssignment>> assignmentsByBus = new HashMap<>();
        for (BusAssignment assignment : assignments) {
            assignmentsByBus.computeIfAbsent(assignment.getBus(), k -> new ArrayList<>()).add(assignment);
        }

        log.info("Processing {} buses with assignments", assignmentsByBus.size());

        int busCount = 0;
        for (Map.Entry<Bus, List<BusAssignment>> entry : assignmentsByBus.entrySet()) {
            if (busCount >= 10) {
                log.info("Reached limit of 10 buses. Skipping remaining {} buses", assignmentsByBus.size() - busCount);
                break;
            }

            Bus bus = entry.getKey();
            List<BusAssignment> busAssignments = entry.getValue();
            Route route = bus.getRoute();

            if (route == null) {
                log.warn("Bus {} has no route assigned - skipping", bus.getName());
                continue;
            }

            log.info("Generating schedules for bus {} on route {}", bus.getName(), route);

            boolean hasMorningDriver = busAssignments.stream()
                    .anyMatch(a -> a.getDriverSchedule() == DriverSchedule.MORNING_AFTERNOON);
            boolean hasEveningDriver = busAssignments.stream()
                    .anyMatch(a -> a.getDriverSchedule() == DriverSchedule.AFTERNOON_EVENING);

            if (hasMorningDriver) {
                allSchedules.addAll(generateSchedulesForBus(bus, route,
                        DriverSchedule.MORNING_AFTERNOON.getStartTime(),
                        DriverSchedule.MORNING_AFTERNOON.getEndTime()));
            }

            if (hasEveningDriver) {
                allSchedules.addAll(generateSchedulesForBus(bus, route,
                        DriverSchedule.AFTERNOON_EVENING.getStartTime(),
                        DriverSchedule.AFTERNOON_EVENING.getEndTime()));
            }

            busCount++;
        }

        if (!allSchedules.isEmpty()) {
            log.info("Saving {} schedule records in batches...", allSchedules.size());
            int batchSize = 1000;
            for (int i = 0; i < allSchedules.size(); i += batchSize) {
                int end = Math.min(i + batchSize, allSchedules.size());
                List<Schedule> batch = allSchedules.subList(i, end);
                scheduleRepository.saveAll(batch);
                log.info("Saved batch {}/{} ({} records)", (i / batchSize) + 1,
                        (allSchedules.size() + batchSize - 1) / batchSize, batch.size());
            }
            log.info("Successfully loaded {} schedule records", allSchedules.size());
        }

        log.info("=== Schedule Data Loader Completed ===");
    }

    private List<Schedule> generateSchedulesForBus(Bus bus, Route route, LocalTime startTime, LocalTime endTime) {
        List<Schedule> schedules = new ArrayList<>();

        List<Destination> forwardPath = ROUTE_PATHS_FORWARD.get(route);
        List<Destination> reversePath = ROUTE_PATHS_REVERSE.get(route);

        LocalDate currentDate = START_DATE;
        while (!currentDate.isAfter(END_DATE)) {
            if (!isRecessOrHoliday(currentDate) && !isSunday(currentDate)) {
                schedules.addAll(generateTripsForDay(bus, route, forwardPath, reversePath,
                        currentDate, startTime, endTime));
            }
            currentDate = currentDate.plusDays(1);
        }

        return schedules;
    }

    private List<Schedule> generateTripsForDay(Bus bus, Route route,
                                               List<Destination> forwardPath,
                                               List<Destination> reversePath,
                                               LocalDate date,
                                               LocalTime startTime,
                                               LocalTime endTime) {
        List<Schedule> schedules = new ArrayList<>();

        LocalTime currentTime = startTime;
        boolean goingForward = true;

        while (true) {
            List<Destination> currentPath = goingForward ? forwardPath : reversePath;
            int totalTripTime = calculateTotalTripTime(currentPath);

            // Check if this trip fits in the remaining time
            if (currentTime.plusMinutes(totalTripTime).isAfter(endTime)) {
                break;
            }

            // Generate all legs of this trip
            LocalTime legDeparture = currentTime;

            for (int i = 0; i < currentPath.size() - 1; i++) {
                Destination from = currentPath.get(i);
                Destination to = currentPath.get(i + 1);

                String key = from.name() + "-" + to.name();
                int travelTime = TRAVEL_TIMES.getOrDefault(key, 30);

                LocalTime arrivalTime = legDeparture.plusMinutes(travelTime);

                Schedule schedule = Schedule.builder()
                        .bus(bus)
                        .fromDestination(from)
                        .toDestination(to)
                        .route(route)
                        .isCompleted(false)
                        .serviceDate(date)
                        .departureTime(legDeparture)
                        .arrivalTime(arrivalTime)
                        .scheduleTripStatus(ScheduleTripStatus.DEPARTURE)
                        .build();

                schedules.add(schedule);
                legDeparture = arrivalTime;
            }

            // Trip ends, add layover at terminal
            int layover = 5 + new Random().nextInt(6); // 5-10 minutes
            currentTime = legDeparture.plusMinutes(layover);

            // Reverse direction for next trip
            goingForward = !goingForward;
        }

        return schedules;
    }

    private int calculateTotalTripTime(List<Destination> path) {
        int totalTime = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Destination from = path.get(i);
            Destination to = path.get(i + 1);
            String key = from.name() + "-" + to.name();
            totalTime += TRAVEL_TIMES.getOrDefault(key, 30);
        }
        return totalTime;
    }

    private boolean isSunday(LocalDate date) {
        return date.getDayOfWeek().getValue() == 7;
    }

    private boolean isRecessOrHoliday(LocalDate date) {
        return RECESS_DATES.contains(date) || PUBLIC_HOLIDAYS.contains(date);
    }
}