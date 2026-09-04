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
    private static final double ABSENTEEISM_RATE = 0.12;       // drivers call in sick
    private static final double DELAY_PROBABILITY = 0.35;
    private static final int THREAD_POOL_SIZE = 8;

    // Peak hour definitions
    private static final LocalTime MORNING_PEAK_START = LocalTime.of(6, 0);
    private static final LocalTime MORNING_PEAK_END = LocalTime.of(9, 0);
    private static final LocalTime AFTERNOON_PEAK_START = LocalTime.of(15, 0);
    private static final LocalTime AFTERNOON_PEAK_END = LocalTime.of(18, 30);

    // Staff travel times (early morning and late afternoon)
    private static final LocalTime STAFF_MORNING_START = LocalTime.of(5, 30);
    private static final LocalTime STAFF_MORNING_END = LocalTime.of(7, 30);
    private static final LocalTime STAFF_AFTERNOON_START = LocalTime.of(16, 0);
    private static final LocalTime STAFF_AFTERNOON_END = LocalTime.of(18, 0);

    // Saturday limited service
    private static final LocalTime SATURDAY_START = LocalTime.of(7, 0);
    private static final LocalTime SATURDAY_END = LocalTime.of(13, 0);

    // Event spike probability (per day) – 5% chance of an event day
    private static final double EVENT_DAY_PROBABILITY = 0.05;
    // Extra passenger multiplier on event days
    private static final double EVENT_EXTRA_MULTIPLIER = 1.3; // 30% more passengers

    // Campus-specific profiles
    // Each route gets a profile that influences passenger count and student/staff ratio
    private static final Map<Route, CampusProfile> CAMPUS_PROFILES = new HashMap<>();
    static {
        // ROUTE_1: DFC-APK – busy commuter route, heavy morning/evening peaks
        CAMPUS_PROFILES.put(Route.ROUTE_1, new CampusProfile(
                0.9,   // base student ratio during peak
                0.3,   // student ratio early morning (staff-heavy)
                0.85,  // student ratio late evening (students going home late)
                1.2,   // evening passenger multiplier (more night activity)
                false  // not a night-owl campus
        ));
        // ROUTE_2: SWC-APB – balanced, moderate evening activity
        CAMPUS_PROFILES.put(Route.ROUTE_2, new CampusProfile(
                0.85,
                0.25,
                0.80,
                1.0,
                false
        ));
        // ROUTE_3: SWC-DFC – direct route, more staff than students
        CAMPUS_PROFILES.put(Route.ROUTE_3, new CampusProfile(
                0.70,
                0.20,
                0.65,
                0.9,
                false
        ));
        // ROUTE_JBS: APK-JBS – high night activity (events, evening classes)
        CAMPUS_PROFILES.put(Route.ROUTE_JBS, new CampusProfile(
                0.88,
                0.28,
                0.92,
                1.4,
                true   // night-owl campus
        ));
    }

    // Statistics
    private final AtomicLong tripCounter = new AtomicLong(0);
    private final AtomicLong tripUserCounter = new AtomicLong(0);
    private final AtomicLong savedTrips = new AtomicLong(0);
    private final AtomicLong savedTripUsers = new AtomicLong(0);
    private final AtomicBoolean generationCompleted = new AtomicBoolean(false);

    private List<UUID> studentIds;
    private List<UUID> staffIds;
    private boolean isInitialized = false;

    @Override
    public void run(String... args) throws Exception {
        log.info("================================================");
        log.info("🚌 STARTING TRIP DATA GENERATION");
        log.info("================================================");

        long startTime = System.currentTimeMillis();

        // Uncomment to enable generation
        if (tripRepository.count() > 0) {
             log.info("✅ Already have {} trips. Skipping generation.", tripRepository.count());
             return;
        }

        initializeUserPools();

        log.info("📊 Loading all schedule legs with bus assignments...");
        var allScheduleLegs = scheduleLegRepository.findAllWithEverythingEagerly();
        log.info("📊 Found {} schedule legs", allScheduleLegs.size());

        var legsByRoute = groupLegsByRoute(allScheduleLegs);
        log.info("📊 Processing {} routes", legsByRoute.size());

        var busAssignmentCache = new HashMap<UUID, BusAssignment>();
        var allBusAssignments = busAssignmentRepository.findAll();
        for (var assignment : allBusAssignments) {
            busAssignmentCache.put(assignment.getBus().getId(), assignment);
        }

        LocalDate startDate = LocalDate.of(2026, 2, 14);
        LocalDate endDate = LocalDate.now();
        List<List<LocalDate>> dateChunks = partitionServiceDates(startDate, endDate, THREAD_POOL_SIZE);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try {
            for (var entry : legsByRoute.entrySet()) {
                if (generationCompleted.get()) break;

                Route route = entry.getKey();
                List<ScheduleLeg> legs = entry.getValue();
                log.info("   🚌 Processing route: {} ({} legs)", route.getLabel(), legs.size());

                Map<DayOfWeek, List<ScheduleLeg>> legsByDay = groupLegsByDayOfWeek(legs);

                List<Future<?>> futures = new ArrayList<>(dateChunks.size());
                for (List<LocalDate> chunk : dateChunks) {
                    futures.add(executor.submit(() ->
                            processDateChunk(route, legsByDay, busAssignmentCache, chunk)));
                }

                for (Future<?> f : futures) {
                    f.get();
                }
            }
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("================================================");
        log.info("✅ COMPLETED!");
        log.info("   📊 Total Trips: {}", savedTrips.get());
        log.info("   📊 Total Trip Users: {}", savedTripUsers.get());
        log.info("   ⏱️  Time: {} seconds", elapsed / 1000);
        log.info("================================================");
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
    // GROUPING HELPERS
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

    private Map<DayOfWeek, List<ScheduleLeg>> groupLegsByDayOfWeek(List<ScheduleLeg> legs) {
        return legs.stream().collect(Collectors.groupingBy(ScheduleLeg::getDayOfWeek));
    }

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
    // PER-THREAD PROCESSING
    // ============================================
    private void processDateChunk(Route route,
                                  Map<DayOfWeek, List<ScheduleLeg>> legsByDay,
                                  Map<UUID, BusAssignment> busAssignmentCache,
                                  List<LocalDate> dates) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        RouteBatchWriter writer = new RouteBatchWriter();
        CampusProfile campusProfile = CAMPUS_PROFILES.get(route);
        if (campusProfile == null) {
            // fallback default
            campusProfile = new CampusProfile(0.85, 0.30, 0.80, 1.0, false);
        }

        // Precompute event days for this chunk (randomly set)
        Set<LocalDate> eventDays = new HashSet<>();
        for (LocalDate date : dates) {
            if (random.nextDouble() < EVENT_DAY_PROBABILITY) {
                eventDays.add(date);
            }
        }

        try {
            for (LocalDate date : dates) {
                if (generationCompleted.get()) break;

                DayOfWeek dayOfWeek = date.getDayOfWeek();
                boolean isSaturday = dayOfWeek == DayOfWeek.SATURDAY;
                boolean isEventDay = eventDays.contains(date);

                List<ScheduleLeg> legsForDay = legsByDay.get(dayOfWeek);
                if (legsForDay == null || legsForDay.isEmpty()) continue;

                for (ScheduleLeg leg : legsForDay) {
                    if (generationCompleted.get()) break;

                    LocalTime depTime = leg.getDepartureTime();

                    // Saturday service hours filter
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

                        // Determine time category
                        boolean isMorningPeak = isInMorningPeak(depTime);
                        boolean isAfternoonPeak = isInAfternoonPeak(depTime);
                        boolean isStaffTime = isStaffTravelTime(depTime);
                        boolean isEvening = depTime.isAfter(LocalTime.of(18, 0)) && depTime.isBefore(LocalTime.of(22, 0));

                        // Calculate passenger count using enhanced method
                        int passengerCount = calculatePassengerCount(
                                capacity, isMorningPeak, isAfternoonPeak,
                                isSaturday, isStaffTime, isEvening,
                                campusProfile, isEventDay, random
                        );

                        if (passengerCount == 0) continue;

                        // Calculate student ratio using enhanced method
                        double studentRatio = calculateStudentRatio(
                                depTime, isSaturday, isStaffTime, isEvening,
                                campusProfile, random
                        );

                        // Passenger composition
                        int studentCount = (int) Math.round(passengerCount * studentRatio);
                        int staffCount = passengerCount - studentCount;

                        // Random variation: ensure we don't exceed passengerCount
                        if (studentCount + staffCount != passengerCount) {
                            // Adjust to match exactly
                            if (studentCount + staffCount > passengerCount) {
                                studentCount = passengerCount - staffCount;
                            } else {
                                staffCount = passengerCount - studentCount;
                            }
                        }

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

                        // Generate trip users
                        List<TripUser> tripUsers = generateTripUsersForTrip(
                                trip, studentCount, staffCount, isSaturday, random);

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
            writer.flush();
        }
    }

    // ============================================
    // ENHANCED PASSENGER COUNT
    // ============================================
    private int calculatePassengerCount(int capacity,
                                        boolean isMorningPeak,
                                        boolean isAfternoonPeak,
                                        boolean isSaturday,
                                        boolean isStaffTime,
                                        boolean isEvening,
                                        CampusProfile profile,
                                        boolean isEventDay,
                                        ThreadLocalRandom random) {
        double baseFactor;

        if (isSaturday) {
            // Saturday: limited service, low occupancy
            if (isStaffTime) {
                baseFactor = random.nextDouble(0.2, 0.4);
            } else {
                baseFactor = random.nextDouble(0.05, 0.2);
            }
        } else if (isMorningPeak) {
            // Morning peak: full buses
            baseFactor = random.nextDouble(0.85, 1.0);
        } else if (isAfternoonPeak) {
            // Afternoon peak: full buses
            baseFactor = random.nextDouble(0.80, 0.98);
        } else if (isStaffTime) {
            // Staff travel time: moderate
            baseFactor = random.nextDouble(0.4, 0.7);
        } else if (isEvening) {
            // Evening: depends on campus night activity
            double eveningBoost = profile.eveningMultiplier;
            baseFactor = random.nextDouble(0.3, 0.6) * eveningBoost;
            baseFactor = Math.min(baseFactor, 0.9); // cap at 90% to avoid unrealistic full buses late
        } else {
            // Off-peak (midday)
            baseFactor = random.nextDouble(0.1, 0.35);
        }

        // Event day: boost occupancy
        if (isEventDay) {
            baseFactor = baseFactor * EVENT_EXTRA_MULTIPLIER;
            baseFactor = Math.min(baseFactor, 1.0); // can't exceed capacity
        }

        int passengerCount = (int) (capacity * baseFactor);
        // Ensure at least 1 passenger for most trips, but allow zero for very off-peak
        if (passengerCount == 0 && baseFactor > 0.02) {
            passengerCount = 1;
        }
        return Math.min(passengerCount, capacity);
    }

    // ============================================
    // ENHANCED STUDENT RATIO CALCULATION
    // ============================================
    private double calculateStudentRatio(LocalTime depTime,
                                         boolean isSaturday,
                                         boolean isStaffTime,
                                         boolean isEvening,
                                         CampusProfile profile,
                                         ThreadLocalRandom random) {
        double baseRatio;

        if (isSaturday) {
            // Saturday: mostly staff or general public, fewer students
            baseRatio = random.nextDouble(0.15, 0.35);
        } else if (isStaffTime) {
            // Early morning or late afternoon staff travel: staff-heavy
            // Use staffStudentRatioEarly from profile
            baseRatio = profile.staffStudentRatioEarly;
            // Add some randomness
            baseRatio += random.nextDouble(-0.05, 0.05);
        } else if (isEvening) {
            // Evening: students heading home or to events
            baseRatio = profile.studentRatioEvening;
            baseRatio += random.nextDouble(-0.05, 0.05);
        } else {
            // Peak or normal hours: mostly students (except direct route)
            baseRatio = profile.baseStudentRatio;
            baseRatio += random.nextDouble(-0.08, 0.08);
        }

        // Clamp to [0.1, 0.95]
        baseRatio = Math.clamp(baseRatio, 0.1, 0.95);
        return baseRatio;
    }

    // ============================================
    // GENERATE TRIP USERS (with given counts)
    // ============================================
    private List<TripUser> generateTripUsersForTrip(Trip trip, int studentCount, int staffCount,
                                                    boolean isSaturday, ThreadLocalRandom random) {
        List<TripUser> tripUsers = new ArrayList<>(studentCount + staffCount);

        // Select students
        List<UUID> selectedStudents = getRandomUserIds(studentIds, studentCount, random);
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

        // Select staff
        List<UUID> selectedStaff = getRandomUserIds(staffIds, staffCount, random);
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
    // UTILITY METHODS (unchanged from original)
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

    private boolean isWithinSaturdayHours(LocalTime time) {
        return !time.isBefore(SATURDAY_START) && !time.isAfter(SATURDAY_END);
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

    // ============================================
    // BATCH WRITER (unchanged)
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

    private void saveTripsWithJdbcBatch(List<Trip> trips) throws Exception {
        String sql = """
                INSERT INTO trip (
                    id, created_at, updated_at, route, status, total_count,
                    departure_time, expected_arrival_time, actual_arrival_time,
                    schedule_leg_bus_assignment_id, bus_assignment_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.batchUpdate(sql, trips, trips.size(), (ps, trip) -> {
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

    /**
     * @param baseStudentRatio       during peak hours
     * @param staffStudentRatioEarly early morning (staff-heavy)
     * @param studentRatioEvening    evening (students)
     * @param eveningMultiplier      factor for passenger count in evening
     * @param isNightOwl             unused but kept for future
     */ // ============================================
        // CAMPUS PROFILE INNER CLASS
        // ============================================
        private record CampusProfile(double baseStudentRatio, double staffStudentRatioEarly, double studentRatioEvening,
                                     double eveningMultiplier, boolean isNightOwl) {
    }
}