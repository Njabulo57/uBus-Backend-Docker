package org.tracker.ubus.ubus.TestDataLoader.Trip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Enum.DriverSchedule;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleRepository;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Order(1) // Run first
@Slf4j
public class TripDataGenerator implements CommandLineRunner {

    private final TripRepository tripRepository;
    private final ScheduleRepository scheduleRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final AtomicInteger savedCount = new AtomicInteger(0);
    private final AtomicInteger skippedCount = new AtomicInteger(0);
    private final AtomicInteger deletedCount = new AtomicInteger(0);

    // Define which buses should have AUTO-GENERATED trips
    // Updated to include DFC 5 and exclude DFC 1
    private static final Set<String> AUTO_GENERATE_BUSES = Set.of(
            "DFC 3", "DFC 5",  // DFC buses (DFC 1 excluded)
            "SWC 1", "SWC 3",  // Some SWC buses
            "JBS 1"            // One JBS bus
    );

    // Buses to EXCLUDE entirely from trip generation
    private static final Set<String> EXCLUDED_BUSES = Set.of(
            "DFC 1"  // DFC 1 is excluded
    );

    // Specify the target date - Monday, July 27, 2026
    private static final LocalDate TARGET_DATE = LocalDate.of(2026, 7, 27);

    // Exclude ROUTE_2 from trip generation
    private static final Set<Route> EXCLUDED_ROUTES = Set.of(Route.ROUTE_2);

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        LocalDate targetDate = TARGET_DATE;
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        log.info("🚀 =============================================");
        log.info("🚀 CREATING TRIPS FOR MONDAY, JULY 27, 2026");
        log.info("🚀 =============================================");
        log.info("📅 Target date: {}", targetDate);
        log.info("📅 Day of week: {}", targetDate.getDayOfWeek());
        log.info("🚫 Excluded routes: {}", EXCLUDED_ROUTES);
        log.info("🚫 Excluded buses: {}", EXCLUDED_BUSES);
        log.info("📋 Auto-generate for buses: {}", AUTO_GENERATE_BUSES);
        log.info("================================================");

        // STEP 1: DELETE existing trips for Monday
        log.info("🗑️ Checking for existing trips on {}...", targetDate);
        List<Trip> existingTrips = tripRepository.findByDepartureTimeBetween(startOfDay, endOfDay);

        if (!existingTrips.isEmpty()) {
            log.info("🗑️ Found {} existing trips on {}. Deleting them...", existingTrips.size(), targetDate);

            // Delete all existing trips for Monday
            tripRepository.deleteAll(existingTrips);
            deletedCount.set(existingTrips.size());

            log.info("✅ Successfully deleted {} trips from {}", deletedCount.get(), targetDate);
        } else {
            log.info("✅ No existing trips found on {}", targetDate);
        }

        // Check if schedules exist
        if (scheduleRepository.count() == 0) {
            log.warn("⚠️ No schedules found! Please run ScheduleDataLoader first.");
            return;
        }

        // Get schedules for the target date
        List<Schedule> targetDateSchedules = scheduleRepository.findByServiceDate(targetDate);

        if (targetDateSchedules.isEmpty()) {
            log.warn("⚠️ No schedules found for {} - generating schedules first", targetDate);
            return;
        }

        log.info("📊 Found {} total schedules for {}", targetDateSchedules.size(), targetDate);

        // STEP 2: Filter out ROUTE_2 schedules
        List<Schedule> filteredByRoute = targetDateSchedules.stream()
                .filter(schedule -> {
                    Route route = schedule.getRoute();
                    if (EXCLUDED_ROUTES.contains(route)) {
                        log.debug("⏭️ Skipping ROUTE_2 schedule: {}", schedule.getId());
                        return false;
                    }
                    return true;
                })
                .toList();

        log.info("📊 After excluding ROUTE_2: {} schedules remaining", filteredByRoute.size());

        // STEP 3: Filter out EXCLUDED buses (DFC 1)
        List<Schedule> filteredByBus = filteredByRoute.stream()
                .filter(schedule -> {
                    Bus bus = schedule.getBus();
                    if (bus == null) {
                        log.warn("⚠️ Schedule {} has no bus assigned", schedule.getId());
                        return false;
                    }
                    if (EXCLUDED_BUSES.contains(bus.getName())) {
                        log.info("⏭️ Skipping excluded bus: {}", bus.getName());
                        return false;
                    }
                    return true;
                })
                .toList();

        log.info("📊 After excluding buses {}: {} schedules remaining", EXCLUDED_BUSES, filteredByBus.size());

        // STEP 4: Filter schedules to only include AUTO-GENERATE buses
        List<Schedule> autoGenerateSchedules = filteredByBus.stream()
                .filter(schedule -> {
                    Bus bus = schedule.getBus();
                    return bus != null && AUTO_GENERATE_BUSES.contains(bus.getName());
                })
                .toList();

        log.info("🚌 Auto-generate schedules: {} (for selected buses)", autoGenerateSchedules.size());
        log.info("⏭️ Manual schedules: {} (drivers will add trips themselves)",
                filteredByBus.size() - autoGenerateSchedules.size());

        if (autoGenerateSchedules.isEmpty()) {
            log.info("✅ No auto-generated trips needed for {}. All trips will be created manually by drivers.", targetDate);
            return;
        }

        // STEP 5: Create new trips
        long startTime = System.currentTimeMillis();

        // Use virtual threads for parallel processing
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (Schedule schedule : autoGenerateSchedules) {
                futures.add(CompletableFuture.runAsync(() ->
                        createTripFromSchedule(schedule, targetDate), executor));
            }

            // Wait for all trips to be created
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        long elapsed = System.currentTimeMillis() - startTime;

        log.info("🎉 =============================================");
        log.info("✅ COMPLETED!");
        log.info("   🗑️ Deleted: {} old trips", deletedCount.get());
        log.info("   ✅ Created: {} new auto-generated trips", savedCount.get());
        log.info("   ⏭️ Skipped: {} trips (no bus assignment)", skippedCount.get());
        log.info("   ⏱️ Time: {} ms", elapsed);
        log.info("🎉 =============================================");

        // Log summary by route
        log.info("📊 Trip Summary by Route:");
        autoGenerateSchedules.stream()
                .map(Schedule::getRoute)
                .distinct()
                .forEach(route -> {
                    long count = autoGenerateSchedules.stream()
                            .filter(s -> s.getRoute() == route)
                            .count();
                    log.info("   {} - {} trips", route.getLabel(), count);
                });

        // Log summary by bus
        log.info("📊 Trip Summary by Bus:");
        autoGenerateSchedules.stream()
                .map(Schedule::getBus)
                .filter(bus -> bus != null)
                .map(Bus::getName)
                .distinct()
                .sorted()
                .forEach(busName -> {
                    long count = autoGenerateSchedules.stream()
                            .filter(s -> s.getBus() != null && s.getBus().getName().equals(busName))
                            .count();
                    log.info("   {} - {} trips", busName, count);
                });

        log.info("💡 Manual trip creation required for these buses:");
        filteredByBus.stream()
                .map(Schedule::getBus)
                .filter(bus -> bus != null && !AUTO_GENERATE_BUSES.contains(bus.getName()))
                .map(Bus::getName)
                .distinct()
                .sorted()
                .forEach(busName -> log.info("   🚌 {}", busName));
    }

    @Transactional
    public void createTripFromSchedule(Schedule schedule, LocalDate serviceDate) {
        try {
            // Get the Bus from the schedule
            Bus bus = schedule.getBus();

            if (bus == null) {
                log.warn("⚠️ Schedule {} has no bus", schedule.getId());
                skippedCount.incrementAndGet();
                return;
            }

            // Determine which shift this schedule belongs to
            DriverSchedule shift = determineShift(schedule.getDepartureTime());

            // Get the bus assignment for this bus and shift
            BusAssignment busAssignment = busAssignmentRepository
                    .findByBusAndDriverSchedule(bus, shift)
                    .orElse(null);

            if (busAssignment == null) {
                log.warn("⚠️ No bus assignment found for bus {} on shift {}", bus.getName(), shift);
                skippedCount.incrementAndGet();
                return;
            }

            // Calculate departure and arrival times
            LocalTime departureTime = schedule.getDepartureTime();
            LocalTime arrivalTime = schedule.getArrivalTime();

            LocalDateTime departureDateTime = LocalDateTime.of(serviceDate, departureTime);
            LocalDateTime arrivalDateTime = LocalDateTime.of(serviceDate, arrivalTime);

            // Determine trip status based on current time
            TripStatus initialStatus = determineTripStatus(departureDateTime, arrivalDateTime);

            // Create the Trip with the bus assignment
            Trip trip = Trip.builder()
                    .route(schedule.getRoute())
                    .schedule(schedule)
                    .status(initialStatus)
                    .busAssignment(busAssignment)
                    .totalCount(0)
                    .departureTime(departureDateTime)
                    .expectedArrivalTime(arrivalDateTime)
                    .build();

            tripRepository.save(trip);
            int saved = savedCount.incrementAndGet();

            if (saved % 10 == 0) {
                log.info("📊 Progress: {} trips created", saved);
            }

        } catch (Exception e) {
            log.error("❌ Failed to create trip for schedule {}: {}", schedule.getId(), e.getMessage());
            skippedCount.incrementAndGet();
        }
    }

    private DriverSchedule determineShift(LocalTime time) {
        LocalTime morningStart = DriverSchedule.MORNING_AFTERNOON.getStartTime();
        LocalTime morningEnd = DriverSchedule.MORNING_AFTERNOON.getEndTime();
        LocalTime eveningStart = DriverSchedule.AFTERNOON_EVENING.getStartTime();
        LocalTime eveningEnd = DriverSchedule.AFTERNOON_EVENING.getEndTime();

        if (!time.isBefore(morningStart) && time.isBefore(morningEnd)) {
            return DriverSchedule.MORNING_AFTERNOON;
        }

        if (!time.isBefore(eveningStart) && time.isBefore(eveningEnd)) {
            return DriverSchedule.AFTERNOON_EVENING;
        }

        if (time.isBefore(morningStart)) {
            return DriverSchedule.MORNING_AFTERNOON;
        }

        return DriverSchedule.AFTERNOON_EVENING;
    }

    private TripStatus determineTripStatus(LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        LocalDateTime now = LocalDateTime.now();

        // If arrival time is in the past, trip is COMPLETE
        if (arrivalDateTime.isBefore(now)) {
            return TripStatus.COMPLETE;
        }

        // If departure time is in the past but arrival is in the future, trip is IN_PROGRESS
        if (departureDateTime.isBefore(now)) {
            return TripStatus.IN_PROGRESS;
        }

        // If departure is in the future, mark it as IN_PROGRESS (ready to go)
        return TripStatus.IN_PROGRESS;
    }
}