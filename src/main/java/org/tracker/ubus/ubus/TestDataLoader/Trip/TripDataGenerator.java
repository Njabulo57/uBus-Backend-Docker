package org.tracker.ubus.ubus.TestDataLoader.Trip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Enum.TripUserStatus;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLeg;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLegBusAssignment;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleLegRepository;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Order(8)
@Component
@RequiredArgsConstructor
public class TripDataGenerator implements CommandLineRunner {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ScheduleLegRepository scheduleLegRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final JdbcTemplate jdbcTemplate;

    // Configuration
    private static final int BATCH_SIZE = 5000;
    private static final int MAX_TRIPS_TO_GENERATE = 1_000_000;
    private static final double ABSENTEEISM_RATE = 0.12;
    private static final double DELAY_PROBABILITY = 0.35;

    // Worker pool size — keep <= your HikariCP maximum-pool-size, since every
    // worker thread holds its own JDBC connection for the duration of a flush.
    private static final int THREAD_POOL_SIZE = 8;

    // Peak hour definitions
    private static final LocalTime MORNING_PEAK_START = LocalTime.of(6, 0);
    private static final LocalTime MORNING_PEAK_END = LocalTime.of(9, 0);
    private static final LocalTime AFTERNOON_PEAK_START = LocalTime.of(15, 0);
    private static final LocalTime AFTERNOON_PEAK_END = LocalTime.of(18, 30);

    // Staff travel times
    private static final LocalTime STAFF_MORNING_START = LocalTime.of(5, 30);
    private static final LocalTime STAFF_MORNING_END = LocalTime.of(7, 30);
    private static final LocalTime STAFF_AFTERNOON_START = LocalTime.of(16, 0);
    private static final LocalTime STAFF_AFTERNOON_END = LocalTime.of(18, 0);

    // Saturday specific hours (limited service)
    private static final LocalTime SATURDAY_START = LocalTime.of(7, 0);
    private static final LocalTime SATURDAY_END = LocalTime.of(13, 0);

    // Statistics — all mutated concurrently, so all Atomic*
    private final AtomicLong tripCounter = new AtomicLong(0);
    private final AtomicLong tripUserCounter = new AtomicLong(0);
    private final AtomicLong savedTrips = new AtomicLong(0);
    private final AtomicLong savedTripUsers = new AtomicLong(0);

    // Flips true once MAX_TRIPS_TO_GENERATE is hit; every worker checks it so
    // all threads wind down instead of one thread silently racing past the cap.
    private final AtomicBoolean generationCompleted = new AtomicBoolean(false);

    // Cached user pools — built once in initializeUserPools() *before* any
    // worker thread starts, and never mutated afterward. Concurrent reads of
    // an unmutated ArrayList/List.get(idx) are safe without synchronization.
    private List<UUID> studentIds;
    private List<UUID> staffIds;
    private boolean isInitialized = false;

    @Override
    public void run(String... args) throws Exception {
        log.info("================================================");
        log.info("🚌 STARTING TRIP DATA GENERATION");
        log.info("================================================");

        long startTime = System.currentTimeMillis();

//        if (tripRepository.count() > 0) {
//            log.info("✅ Already have {} trips. Skipping generation.", tripRepository.count());
//            return;
//        }
//
//        // 1. Initialize user pools (single-threaded, before any parallel work)
//        initializeUserPools();
//
//        // 2. Get all schedule legs with EVERYTHING eagerly fetched
//        log.info("📊 Loading all schedule legs with bus assignments...");
//        var allScheduleLegs = scheduleLegRepository.findAllWithEverythingEagerly();
//        log.info("📊 Found {} schedule legs", allScheduleLegs.size());
//
//        // 3. Group legs by route
//        var legsByRoute = groupLegsByRoute(allScheduleLegs);
//        log.info("📊 Processing {} routes", legsByRoute.size());
//
//        // 4. Pre-load bus assignments cache for faster access — built once,
//        // read-only from here on, so it's safe to share across worker threads.
//        var busAssignmentCache = new HashMap<UUID, BusAssignment>();
//        var allBusAssignments = busAssignmentRepository.findAll();
//        for (var assignment : allBusAssignments) {
//            busAssignmentCache.put(assignment.getBus().getId(), assignment);
//        }
//
//        // 5. Precompute the date range + chunking ONCE. Every route shares the
//        // same [startDate, endDate] window, so there's no need to recompute
//        // this per route.
//        LocalDate startDate = LocalDate.of(2026, 2, 14);
//        LocalDate endDate = LocalDate.now();
//        List<List<LocalDate>> dateChunks = partitionServiceDates(startDate, endDate, THREAD_POOL_SIZE);
//
//        // 6. Process each route. Routes run one at a time (so logging stays
//        // readable and peak DB connection usage stays bounded), but within a
//        // route the date range is split across THREAD_POOL_SIZE workers.
//        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
//        try {
//            for (var entry : legsByRoute.entrySet()) {
//                if (generationCompleted.get()) {
//                    log.warn("⚠️ Reached maximum trip limit. Stopping.");
//                    break;
//                }
//
//                Route route = entry.getKey();
//                List<ScheduleLeg> legs = entry.getValue();
//                log.info("   🚌 Processing route: {} ({} legs)", route.getLabel(), legs.size());
//
//                Map<DayOfWeek, List<ScheduleLeg>> legsByDay = groupLegsByDayOfWeek(legs);
//
//                List<Future<?>> futures = new ArrayList<>(dateChunks.size());
//                for (List<LocalDate> chunk : dateChunks) {
//                    futures.add(executor.submit(() ->
//                            processDateChunk(route, legsByDay, busAssignmentCache, chunk)));
//                }
//
//                // Wait for this route to finish before starting the next one.
//                // Propagates any worker exception immediately instead of
//                // silently swallowing it.
//                for (Future<?> f : futures) {
//                    f.get();
//                }
//            }
//        } finally {
//            executor.shutdown();
//            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
//                executor.shutdownNow();
//            }
//        }
//
//        long elapsed = System.currentTimeMillis() - startTime;
//
//        log.info("================================================");
//        log.info("✅ COMPLETED!");
//        log.info("   📊 Total Trips: {}", savedTrips.get());
//        log.info("   📊 Total Trip Users: {}", savedTripUsers.get());
//        log.info("   ⏱️  Time: {} seconds", elapsed / 1000);
//        log.info("================================================");
    }

    // ============================================
    // INITIALIZE USER POOLS
    // ============================================
    private void initializeUserPools() {
        if (isInitialized) return;

        log.info("📊 Loading user pools...");

        var allStudents = userRepository.findByRole(UserRole.STUDENT);
        var allStaff = userRepository.findByRole(UserRole.STAFF);

        studentIds = allStudents.stream().map(User::getId).collect(Collectors.toList());
        staffIds = allStaff.stream().map(User::getId).collect(Collectors.toList());

        Collections.shuffle(studentIds, new Random(ThreadLocalRandom.current().nextLong()));
        Collections.shuffle(staffIds, new Random(ThreadLocalRandom.current().nextLong()));

        log.info("   👨‍🎓 Students: {}", studentIds.size());
        log.info("   👨‍🏫 Staff: {}", staffIds.size());

        isInitialized = true;
    }

    // ============================================
    // GROUP LEGS BY ROUTE
    // ============================================
    private Map<Route, List<ScheduleLeg>> groupLegsByRoute(List<ScheduleLeg> legs) {
        Map<Route, List<ScheduleLeg>> routeMap = new HashMap<>();

        for (var leg : legs) {
            var schedule = leg.getSchedule();
            if (schedule == null) continue;
            routeMap.computeIfAbsent(schedule.getRoute(), k -> new ArrayList<>()).add(leg);
        }

        for (var entry : routeMap.entrySet()) {
            entry.getValue().sort(Comparator.comparing(ScheduleLeg::getDepartureTime));
        }

        return routeMap;
    }

    // ============================================
    // GROUP LEGS BY DAY OF WEEK (done once per route instead of re-filtering
    // the full leg list for every single date)
    // ============================================
    private Map<DayOfWeek, List<ScheduleLeg>> groupLegsByDayOfWeek(List<ScheduleLeg> legs) {
        return legs.stream().collect(Collectors.groupingBy(ScheduleLeg::getDayOfWeek));
    }

    // ============================================
    // PARTITION THE SERVICE-DATE RANGE INTO ROUGHLY EQUAL CHUNKS
    // (Sundays are dropped up front since there's no service at all — this
    // keeps chunk sizes balanced by actual work, not calendar days.)
    // ============================================
    private List<List<LocalDate>> partitionServiceDates(LocalDate startDate, LocalDate endDate, int numChunks) {
        List<LocalDate> serviceDates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                serviceDates.add(date);
            }
        }

        List<List<LocalDate>> chunks = new ArrayList<>();
        int total = serviceDates.size();
        if (total == 0) return chunks;

        int chunkSize = (int) Math.ceil((double) total / numChunks);
        for (int i = 0; i < total; i += chunkSize) {
            chunks.add(serviceDates.subList(i, Math.min(i + chunkSize, total)));
        }
        return chunks;
    }

    // ============================================
    // PROCESS A CHUNK OF DATES FOR ONE ROUTE — runs on a worker thread.
    // Everything mutable here (the RouteBatchWriter's lists) is LOCAL to this
    // thread. Nothing is shared except read-only lookups (legsByDay,
    // busAssignmentCache, studentIds/staffIds) and the Atomic counters, so
    // there's no interleaving that can split a trip from its trip-users
    // across batches or threads.
    // ============================================
    private void processDateChunk(Route route,
                                  Map<DayOfWeek, List<ScheduleLeg>> legsByDay,
                                  Map<UUID, BusAssignment> busAssignmentCache,
                                  List<LocalDate> dates) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        RouteBatchWriter writer = new RouteBatchWriter();

        try {
            for (LocalDate date : dates) {
                if (generationCompleted.get()) break;

                DayOfWeek dayOfWeek = date.getDayOfWeek();
                boolean isSaturday = dayOfWeek == DayOfWeek.SATURDAY;

                List<ScheduleLeg> legsForDay = legsByDay.get(dayOfWeek);
                if (legsForDay == null || legsForDay.isEmpty()) continue;

                for (ScheduleLeg leg : legsForDay) {
                    if (generationCompleted.get()) break;

                    LocalTime depTime = leg.getDepartureTime();

                    // SATURDAY: Only run if within Saturday service hours (07:00 - 13:00)
                    if (isSaturday && !isWithinSaturdayHours(depTime)) {
                        continue;
                    }

                    // Driver absenteeism
                    if (random.nextDouble() < ABSENTEEISM_RATE) {
                        continue;
                    }

                    var busAssignments = leg.getBusesAssigned();
                    if (busAssignments == null || busAssignments.isEmpty()) continue;

                    for (ScheduleLegBusAssignment scheduleLegAssignment : busAssignments) {
                        Bus bus = scheduleLegAssignment.getBus();
                        int capacity = bus.getCapacity();

                        BusAssignment busAssignment = busAssignmentCache.get(bus.getId());
                        if (busAssignment == null) {
                            throw new RuntimeException("Bus assignment not found for bus ID: " + bus.getId());
                        }

                        boolean isMorningPeak = isInMorningPeak(depTime);
                        boolean isAfternoonPeak = isInAfternoonPeak(depTime);
                        boolean isStaffTime = isStaffTravelTime(depTime);

                        int passengerCount = calculatePassengerCount(
                                capacity, isMorningPeak, isAfternoonPeak, isSaturday, isStaffTime
                        );

                        if (passengerCount == 0) continue;

                        boolean isDelayed = random.nextDouble() < DELAY_PROBABILITY;
                        int delayMinutes = isDelayed ? calculateDelay(random, isMorningPeak, isAfternoonPeak) : 0;

                        Trip trip = Trip.builder()
                                .id(UUID.randomUUID())
                                .route(route)
                                .scheduleLegBusAssignment(scheduleLegAssignment)
                                .busAssignment(busAssignment)
                                .status(TripStatus.COMPLETE)
                                .totalCount(passengerCount)
                                .departureTime(createDateTime(date, depTime, isDelayed, delayMinutes))
                                .expectedArrivalTime(createDateTime(date, leg.getArrivalTime(), false, 0))
                                .actualArrivalTime(createDateTime(date, leg.getArrivalTime(), isDelayed, delayMinutes))
                                .build();

                        List<TripUser> tripUsers = generateTripUsersForTrip(
                                trip, passengerCount, isSaturday, isStaffTime, random);

                        writer.add(trip, tripUsers);

                        long counted = tripCounter.incrementAndGet();
                        if (counted >= MAX_TRIPS_TO_GENERATE) {
                            generationCompleted.set(true);
                            break;
                        }
                    }
                }
            }
        } finally {
            // Always flush whatever this worker accumulated, even on early
            // exit via generationCompleted or an exception below.
            writer.flush();
        }
    }

    // ============================================
    // HELPER: SATURDAY HOURS CHECK
    // ============================================
    private boolean isWithinSaturdayHours(LocalTime time) {
        return !time.isBefore(SATURDAY_START) && !time.isAfter(SATURDAY_END);
    }

    // ============================================
    // GENERATE TRIP USERS
    // ============================================
    private List<TripUser> generateTripUsersForTrip(Trip trip, int passengerCount, boolean isSaturday,
                                                    boolean isStaffTime, ThreadLocalRandom random) {
        List<TripUser> tripUsers = new ArrayList<>(passengerCount);

        double studentRatio = calculateStudentRatio(isSaturday, isStaffTime, random);
        int studentCount = (int) (passengerCount * studentRatio);
        int staffCount = passengerCount - studentCount;

        List<UUID> selectedStudents = getRandomUserIds(studentIds, studentCount, random);
        List<UUID> selectedStaff = getRandomUserIds(staffIds, staffCount, random);

        for (var userId : selectedStudents) {
            tripUsers.add(TripUser.builder()
                    .id(UUID.randomUUID())
                    .user(User.builder().id(userId).build())
                    .trip(trip)
                    .status(getRandomTripUserStatus(random))
                    .isFirstTrip(random.nextBoolean())
                    .build());
            tripUserCounter.incrementAndGet();
        }

        for (var userId : selectedStaff) {
            tripUsers.add(TripUser.builder()
                    .id(UUID.randomUUID())
                    .user(User.builder().id(userId).build())
                    .trip(trip)
                    .status(getRandomTripUserStatus(random))
                    .isFirstTrip(random.nextBoolean())
                    .build());
            tripUserCounter.incrementAndGet();
        }

        return tripUsers;
    }

    // ============================================
    // RANDOM SAMPLING WITHOUT REPLACEMENT — avoids copying + fully shuffling
    // the entire pool (thousands of UUIDs) just to take a handful of picks.
    //   - Small sample relative to pool: reservoir-style index sampling, O(count).
    //   - Large sample relative to pool: partial Fisher–Yates on a local copy.
    // `pool` (studentIds/staffIds) is read-only and shared; `random` is the
    // caller's own ThreadLocalRandom, so this is safe to call concurrently.
    // ============================================
    private List<UUID> getRandomUserIds(List<UUID> pool, int count, ThreadLocalRandom random) {
        if (pool.isEmpty() || count <= 0) return Collections.emptyList();
        int size = pool.size();
        int actualCount = Math.min(count, size);

        if (actualCount > size / 2) {
            List<UUID> working = new ArrayList<>(pool);
            for (int i = 0; i < actualCount; i++) {
                Collections.swap(working, i, i + random.nextInt(size - i));
            }
            return working.subList(0, actualCount);
        }

        Set<Integer> pickedIndexes = new HashSet<>();
        List<UUID> result = new ArrayList<>(actualCount);
        while (pickedIndexes.size() < actualCount) {
            int idx = random.nextInt(size);
            if (pickedIndexes.add(idx)) {
                result.add(pool.get(idx));
            }
        }
        return result;
    }

    private TripUserStatus getRandomTripUserStatus(ThreadLocalRandom random) {
        double r = random.nextDouble();
        if (r < 0.15) return TripUserStatus.CONTINUED_TO_NEXT;
        else if (r < 0.85) return TripUserStatus.EXITED;
        else return TripUserStatus.IN_BUS;
    }

    // ============================================
    // PER-THREAD BATCH WRITER
    // Local ArrayLists, never shared across threads — a trip and its
    // trip-users always flush together from the same worker, so there's no
    // possible interleaving that saves trip-users before their trip commits.
    // JdbcTemplate itself is stateless/thread-safe: each call just pulls a
    // connection from the pool, so concurrent flush() calls from different
    // workers are safe as long as THREAD_POOL_SIZE <= your DB pool size.
    // ============================================
    private class RouteBatchWriter {
        private final List<Trip> trips = new ArrayList<>(BATCH_SIZE);
        private final List<TripUser> tripUsers = new ArrayList<>(BATCH_SIZE * 20);

        void add(Trip trip, List<TripUser> usersForTrip) {
            trips.add(trip);
            tripUsers.addAll(usersForTrip);

            if (trips.size() >= BATCH_SIZE) {
                flush();
            }
        }

        void flush() {
            if (trips.isEmpty()) return;

            try {
                // Trips first, then trip-users, so the FK always resolves.
                saveTripsWithJdbcBatch(trips);
                if (!tripUsers.isEmpty()) {
                    saveTripUsersWithJdbcBatch(tripUsers);
                }

                long totalSaved = savedTrips.addAndGet(trips.size());
                savedTripUsers.addAndGet(tripUsers.size());

                if (totalSaved % 50000 < BATCH_SIZE) {
                    log.info("      💾 {} trips, {} trip users saved", savedTrips.get(), savedTripUsers.get());
                }
            } catch (Exception e) {
                log.error("❌ Error flushing trip batch: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            } finally {
                trips.clear();
                tripUsers.clear();
            }
        }
    }

    // ============================================
    // SAVE TRIPS WITH JDBC BATCH
    // ============================================
    private void saveTripsWithJdbcBatch(List<Trip> trips) throws Exception {
        String sql = """
                INSERT INTO trip (
                    id, created_at, updated_at, route, status, total_count,
                    departure_time, expected_arrival_time, actual_arrival_time,
                    schedule_leg_bus_assignment_id, bus_assignment_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(sql, trips, trips.size(), (ps, trip) -> {
            if (trip == null) {
                log.error("❌ NULL TRIP in batch!");
                return;
            }
            ps.setObject(1, trip.getId());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, trip.getRoute().name());
            ps.setString(5, trip.getStatus().name());
            ps.setInt(6, trip.getTotalCount());
            ps.setTimestamp(7, trip.getDepartureTime() != null ? Timestamp.valueOf(trip.getDepartureTime()) : null);
            ps.setTimestamp(8, trip.getExpectedArrivalTime() != null ? Timestamp.valueOf(trip.getExpectedArrivalTime()) : null);
            ps.setTimestamp(9, trip.getActualArrivalTime() != null ? Timestamp.valueOf(trip.getActualArrivalTime()) : null);
            ps.setObject(10, trip.getScheduleLegBusAssignment() != null ? trip.getScheduleLegBusAssignment().getId() : null);
            ps.setObject(11, trip.getBusAssignment() != null ? trip.getBusAssignment().getId() : null);
        });
    }

    // ============================================
    // SAVE TRIP USERS WITH JDBC BATCH
    // ============================================
    private void saveTripUsersWithJdbcBatch(List<TripUser> tripUsers) throws Exception {
        if (tripUsers.isEmpty()) return;

        String sql = """
                INSERT INTO trip_user (
                    id, created_at, updated_at, status, is_first_trip, user_id, trip_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        int total = tripUsers.size();
        int batchSize = 10000;

        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            List<TripUser> batch = tripUsers.subList(i, end);

            jdbcTemplate.batchUpdate(sql, batch, batch.size(), (ps, tu) -> {
                if (tu == null || tu.getTrip() == null) {
                    log.error("❌ NULL TripUser or Trip in batch!");
                    return;
                }
                ps.setObject(1, tu.getId());
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(4, tu.getStatus().name());
                ps.setBoolean(5, tu.isFirstTrip());
                ps.setObject(6, tu.getUser().getId());
                ps.setObject(7, tu.getTrip().getId());
            });
        }
    }

    // ============================================
    // PASSENGER COUNT CALCULATION
    // ============================================
    private int calculatePassengerCount(int capacity, boolean isMorningPeak, boolean isAfternoonPeak,
                                        boolean isSaturday, boolean isStaffTime) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        if (isSaturday) {
            if (isStaffTime) {
                return random.nextInt((int) (capacity * 0.2), (int) (capacity * 0.4));
            }
            return random.nextInt(3, (int) (capacity * 0.15));
        }

        if (isMorningPeak || isAfternoonPeak) {
            return random.nextInt((int) (capacity * 0.85), capacity);
        }

        if (isStaffTime) {
            return random.nextInt((int) (capacity * 0.4), (int) (capacity * 0.7));
        }

        return random.nextInt((int) (capacity * 0.1), (int) (capacity * 0.35));
    }

    private double calculateStudentRatio(boolean isSaturday, boolean isStaffTime, ThreadLocalRandom random) {
        if (isSaturday) {
            return random.nextDouble(0.15, 0.35);
        }

        if (isStaffTime) {
            return random.nextDouble(0.25, 0.45);
        }

        return random.nextDouble(0.80, 0.95);
    }

    private boolean isInMorningPeak(LocalTime time) {
        return time.isAfter(MORNING_PEAK_START) && time.isBefore(MORNING_PEAK_END);
    }

    private boolean isInAfternoonPeak(LocalTime time) {
        return time.isAfter(AFTERNOON_PEAK_START) && time.isBefore(AFTERNOON_PEAK_END);
    }

    private boolean isStaffTravelTime(LocalTime time) {
        return (time.isAfter(STAFF_MORNING_START) && time.isBefore(STAFF_MORNING_END)) ||
                (time.isAfter(STAFF_AFTERNOON_START) && time.isBefore(STAFF_AFTERNOON_END));
    }

    private int calculateDelay(ThreadLocalRandom random, boolean isMorningPeak, boolean isAfternoonPeak) {
        if (isMorningPeak || isAfternoonPeak) return random.nextInt(5, 26);
        return random.nextInt(2, 11);
    }

    private LocalDateTime createDateTime(LocalDate date, LocalTime time, boolean isDelayed, int delayMinutes) {
        LocalDateTime base = date.atTime(time);
        return isDelayed ? base.plusMinutes(delayMinutes) : base;
    }
}