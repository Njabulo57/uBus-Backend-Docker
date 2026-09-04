package org.tracker.ubus.ubus.TestDataLoader.Schedule;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleDatesExcluded;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLeg;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLegBusAssignment;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Enum.DaysExcludedReason;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleIncludedRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleLegAssignmentRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleLegRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleRepository;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Order(7)
@Component
@RequiredArgsConstructor
public class ScheduleGenerationDestData implements CommandLineRunner {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleLegRepository scheduleLegRepository;
    private final ScheduleLegAssignmentRepository scheduleLegBusAssignmentRepository;
    private final ScheduleIncludedRepository scheduleDatesExcludedRepository;
    private final BusRepository busRepository;

    private static final LocalDate YEAR_START = LocalDate.of(2026, 2, 14);
    private static final LocalDate YEAR_END = LocalDate.of(2026, 11, 30);

    // ============================================
    // ROUTE 1 WAVES — DFC-APK (Monday - Friday ONLY)
    // ============================================
    private static final List<RouteWave> ROUTE1_WAVES = List.of(
            new RouteWave(LocalTime.of(5, 0), route1Legs()),
            new RouteWave(LocalTime.of(5, 30), route1Legs()),
            new RouteWave(LocalTime.of(6, 0), route1Legs()),
            new RouteWave(LocalTime.of(6, 30), route1Legs()),
            new RouteWave(LocalTime.of(7, 0), route1Legs()),
            new RouteWave(LocalTime.of(7, 30), route1Legs()),
            new RouteWave(LocalTime.of(8, 0), route1Legs()),
            new RouteWave(LocalTime.of(8, 30), route1Legs()),
            new RouteWave(LocalTime.of(9, 0), route1Legs()),
            new RouteWave(LocalTime.of(9, 30), route1Legs()),
            new RouteWave(LocalTime.of(10, 0), route1Legs()),
            new RouteWave(LocalTime.of(10, 30), route1Legs()),
            new RouteWave(LocalTime.of(11, 0), route1Legs()),
            new RouteWave(LocalTime.of(11, 30), route1Legs()),
            new RouteWave(LocalTime.of(12, 0), route1Legs()),
            new RouteWave(LocalTime.of(12, 30), route1Legs()),
            new RouteWave(LocalTime.of(13, 0), route1Legs()),
            new RouteWave(LocalTime.of(13, 30), route1Legs()),
            new RouteWave(LocalTime.of(14, 0), route1Legs()),
            new RouteWave(LocalTime.of(14, 30), route1Legs()),
            new RouteWave(LocalTime.of(15, 0), route1Legs()),
            new RouteWave(LocalTime.of(15, 30), route1Legs()),
            new RouteWave(LocalTime.of(16, 0), route1Legs()),
            new RouteWave(LocalTime.of(16, 30), route1Legs()),
            new RouteWave(LocalTime.of(17, 0), route1Legs()),
            new RouteWave(LocalTime.of(17, 30), route1Legs()),
            new RouteWave(LocalTime.of(18, 0), route1Legs()),
            new RouteWave(LocalTime.of(18, 30), route1Legs()),
            new RouteWave(LocalTime.of(19, 0), route1Legs()),
            new RouteWave(LocalTime.of(19, 30), route1Legs()),
            new RouteWave(LocalTime.of(20, 0), route1Legs()),
            new RouteWave(LocalTime.of(20, 30), route1Legs()),
            new RouteWave(LocalTime.of(21, 0), route1Legs()),
            new RouteWave(LocalTime.of(21, 30), route1Legs())
    );

    private static List<LegSegment> route1Legs() {
        return List.of(
                new LegSegment(Destination.DFC, Destination.APB, 30),
                new LegSegment(Destination.APB, Destination.APK, 15),
                new LegSegment(Destination.APK, Destination.APB, 15),
                new LegSegment(Destination.APB, Destination.DFC, 30)
        );
    }

    // ============================================
    // ROUTE 2 WAVES — SWC-APB (Monday - Friday ONLY)
    // ============================================
    private static final List<RouteWave> ROUTE2_WAVES = List.of(
            new RouteWave(LocalTime.of(5, 0), route2Legs()),
            new RouteWave(LocalTime.of(5, 30), route2Legs()),
            new RouteWave(LocalTime.of(6, 0), route2Legs()),
            new RouteWave(LocalTime.of(6, 30), route2Legs()),
            new RouteWave(LocalTime.of(7, 0), route2Legs()),
            new RouteWave(LocalTime.of(7, 30), route2Legs()),
            new RouteWave(LocalTime.of(8, 0), route2Legs()),
            new RouteWave(LocalTime.of(8, 30), route2Legs()),
            new RouteWave(LocalTime.of(9, 0), route2Legs()),
            new RouteWave(LocalTime.of(9, 30), route2Legs()),
            new RouteWave(LocalTime.of(10, 0), route2Legs()),
            new RouteWave(LocalTime.of(10, 30), route2Legs()),
            new RouteWave(LocalTime.of(11, 0), route2Legs()),
            new RouteWave(LocalTime.of(11, 30), route2Legs()),
            new RouteWave(LocalTime.of(12, 0), route2Legs()),
            new RouteWave(LocalTime.of(12, 30), route2Legs()),
            new RouteWave(LocalTime.of(13, 0), route2Legs()),
            new RouteWave(LocalTime.of(13, 30), route2Legs()),
            new RouteWave(LocalTime.of(14, 0), route2Legs()),
            new RouteWave(LocalTime.of(14, 30), route2Legs()),
            new RouteWave(LocalTime.of(15, 0), route2Legs()),
            new RouteWave(LocalTime.of(15, 30), route2Legs()),
            new RouteWave(LocalTime.of(16, 0), route2Legs()),
            new RouteWave(LocalTime.of(16, 30), route2Legs()),
            new RouteWave(LocalTime.of(17, 0), route2Legs()),
            new RouteWave(LocalTime.of(17, 30), route2Legs()),
            new RouteWave(LocalTime.of(18, 0), route2Legs()),
            new RouteWave(LocalTime.of(18, 30), route2Legs()),
            new RouteWave(LocalTime.of(19, 0), route2Legs()),
            new RouteWave(LocalTime.of(19, 30), route2Legs()),
            new RouteWave(LocalTime.of(20, 0), route2Legs()),
            new RouteWave(LocalTime.of(20, 30), route2Legs()),
            new RouteWave(LocalTime.of(21, 0), route2Legs()),
            new RouteWave(LocalTime.of(21, 30), route2Legs())
    );

    private static List<LegSegment> route2Legs() {
        return List.of(
                new LegSegment(Destination.SWC, Destination.APK, 40),
                new LegSegment(Destination.APK, Destination.APB, 15),
                new LegSegment(Destination.APB, Destination.APK, 15),
                new LegSegment(Destination.APK, Destination.SWC, 40)
        );
    }

    // ============================================
    // ROUTE 3 WAVES — SWC-DFC (Monday - Friday ONLY)
    // ============================================
    private static final List<RouteWave> ROUTE3_WAVES = List.of(
            new RouteWave(LocalTime.of(5, 0), route3Legs()),
            new RouteWave(LocalTime.of(5, 30), route3Legs()),
            new RouteWave(LocalTime.of(6, 0), route3Legs()),
            new RouteWave(LocalTime.of(6, 30), route3Legs()),
            new RouteWave(LocalTime.of(7, 0), route3Legs()),
            new RouteWave(LocalTime.of(7, 30), route3Legs()),
            new RouteWave(LocalTime.of(8, 0), route3Legs()),
            new RouteWave(LocalTime.of(8, 30), route3Legs()),
            new RouteWave(LocalTime.of(9, 0), route3Legs()),
            new RouteWave(LocalTime.of(9, 30), route3Legs()),
            new RouteWave(LocalTime.of(10, 0), route3Legs()),
            new RouteWave(LocalTime.of(10, 30), route3Legs()),
            new RouteWave(LocalTime.of(11, 0), route3Legs()),
            new RouteWave(LocalTime.of(11, 30), route3Legs()),
            new RouteWave(LocalTime.of(12, 0), route3Legs()),
            new RouteWave(LocalTime.of(12, 30), route3Legs()),
            new RouteWave(LocalTime.of(13, 0), route3Legs()),
            new RouteWave(LocalTime.of(13, 30), route3Legs()),
            new RouteWave(LocalTime.of(14, 0), route3Legs()),
            new RouteWave(LocalTime.of(14, 30), route3Legs()),
            new RouteWave(LocalTime.of(15, 0), route3Legs()),
            new RouteWave(LocalTime.of(15, 30), route3Legs()),
            new RouteWave(LocalTime.of(16, 0), route3Legs()),
            new RouteWave(LocalTime.of(16, 30), route3Legs()),
            new RouteWave(LocalTime.of(17, 0), route3Legs()),
            new RouteWave(LocalTime.of(17, 30), route3Legs()),
            new RouteWave(LocalTime.of(18, 0), route3Legs()),
            new RouteWave(LocalTime.of(18, 30), route3Legs()),
            new RouteWave(LocalTime.of(19, 0), route3Legs()),
            new RouteWave(LocalTime.of(19, 30), route3Legs()),
            new RouteWave(LocalTime.of(20, 0), route3Legs()),
            new RouteWave(LocalTime.of(20, 30), route3Legs()),
            new RouteWave(LocalTime.of(21, 0), route3Legs()),
            new RouteWave(LocalTime.of(21, 30), route3Legs())
    );

    private static List<LegSegment> route3Legs() {
        return List.of(
                new LegSegment(Destination.SWC, Destination.DFC, 40),
                new LegSegment(Destination.DFC, Destination.SWC, 40)
        );
    }

    // ============================================
    // ROUTE JBS WAVES — APK-JBS (Monday - Friday)
    // ============================================
    private static final List<RouteWave> ROUTE_JBS_WAVES = List.of(
            new RouteWave(LocalTime.of(5, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(5, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(6, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(6, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(7, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(7, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(8, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(8, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(9, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(9, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(10, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(10, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(11, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(11, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(12, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(12, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(13, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(13, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(14, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(14, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(15, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(15, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(16, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(16, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(17, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(17, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(18, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(18, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(19, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(19, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(20, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(20, 30), routeJBSLegs()),
            new RouteWave(LocalTime.of(21, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(21, 30), routeJBSLegs())
    );

    private static List<LegSegment> routeJBSLegs() {
        return List.of(
                new LegSegment(Destination.APK, Destination.APB, 15),
                new LegSegment(Destination.APB, Destination.JBS, 15),
                new LegSegment(Destination.JBS, Destination.APB, 15),
                new LegSegment(Destination.APB, Destination.APK, 15)
        );
    }

    // ============================================
    // SATURDAY WAVES — ONLY JBS ROUTE
    // ============================================
    private static final List<RouteWave> ROUTE_JBS_SATURDAY_WAVES = List.of(
            new RouteWave(LocalTime.of(7, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(8, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(9, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(10, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(11, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(12, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(13, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(14, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(15, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(16, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(17, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(18, 0), routeJBSLegs()),
            new RouteWave(LocalTime.of(19, 0), routeJBSLegs())
    );

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== Populating Schedule Data for 14 Feb - 30 Nov 2026 ===");
        log.info("=== Monday-Friday: All routes | Saturday: JBS only ===");

        if (scheduleRepository.count() > 0) {
            log.info("✅ Schedules already exist. Skipping generation.");
            return;
        }

        var allOperationalBuses = busRepository
                .findByOperationalStatusAndIsActiveTrue(BusOperationalStatus.OPERATIONAL);

        if (allOperationalBuses.isEmpty()) {
            log.warn("No operational buses found.");
            return;
        }
        log.info("Found {} operational buses", allOperationalBuses.size());

        // ============================================
        // CORRECT: Filter by ROUTE enum
        // ============================================
        var route1Buses = allOperationalBuses.stream()
                .filter(bus -> bus.getRoute() == Route.ROUTE_1)
                .sorted(Comparator.comparing(Bus::getName))
                .collect(Collectors.toList());

        var route2Buses = allOperationalBuses.stream()
                .filter(bus -> bus.getRoute() == Route.ROUTE_2)
                .sorted(Comparator.comparing(Bus::getName))
                .collect(Collectors.toList());

        var route3Buses = allOperationalBuses.stream()
                .filter(bus -> bus.getRoute() == Route.ROUTE_3)
                .sorted(Comparator.comparing(Bus::getName))
                .collect(Collectors.toList());

        var jbsBuses = allOperationalBuses.stream()
                .filter(bus -> bus.getRoute() == Route.ROUTE_JBS)
                .sorted(Comparator.comparing(Bus::getName))
                .collect(Collectors.toList());

        log.info("=== Bus Distribution by Route ===");
        log.info("ROUTE_1 (DFC-APK): {}", route1Buses.stream().map(Bus::getName).collect(Collectors.joining(", ")));
        log.info("ROUTE_2 (SWC-APB): {}", route2Buses.stream().map(Bus::getName).collect(Collectors.joining(", ")));
        log.info("ROUTE_3 (SWC-DFC): {}", route3Buses.stream().map(Bus::getName).collect(Collectors.joining(", ")));
        log.info("ROUTE_JBS (APK-JBS): {}", jbsBuses.stream().map(Bus::getName).collect(Collectors.joining(", ")));

        // ============================================
        // ROUTE 1: Use first 3 buses
        // ============================================
        if (route1Buses.size() >= 3) {
            var buses = route1Buses.subList(0, 3);
            createRoute1Schedule(buses);
        } else {
            log.warn("Not enough buses for Route 1. Found: {}, Need: 3", route1Buses.size());
        }

        // ============================================
        // ROUTE 2: Use first 5 buses
        // ============================================
        if (route2Buses.size() >= 5) {
            var buses = route2Buses.subList(0, 5);
            createRoute2Schedule(buses);
        } else {
            log.warn("Not enough buses for Route 2. Found: {}, Need: 5", route2Buses.size());
        }

        // ============================================
        // ROUTE 3: Use first 4 buses
        // ============================================
        if (route3Buses.size() >= 4) {
            var buses = route3Buses.subList(0, 4);
            createRoute3Schedule(buses);
        } else {
            log.warn("Not enough buses for Route 3. Found: {}, Need: 4", route3Buses.size());
        }

        // ============================================
        // ROUTE JBS: Use first 2 buses
        // ============================================
        if (jbsBuses.size() >= 2) {
            var buses = jbsBuses.subList(0, 2);
            createRouteJBSSchedule(buses);
            createRouteJBSSaturdaySchedule(buses);
        } else {
            log.warn("Not enough buses for Route JBS. Found: {}, Need: 2", jbsBuses.size());
        }

        log.info("=== Schedule Data Population Complete ===");
    }

    // ============================================
    // ROUTE 1: DFC ↔ APB ↔ APK (3 buses)
    // ============================================
    private void createRoute1Schedule(List<Bus> buses) {
        log.info("Creating Route 1 Schedule (DFC ↔ APB ↔ APK) - {} buses", buses.size());

        var schedule = Schedule.builder()
                .route(Route.ROUTE_1)
                .validFromDate(YEAR_START)
                .validToDate(YEAR_END)
                .build();
        var savedSchedule = scheduleRepository.save(schedule);

        var excludedDates = createExcludedDatesForYear(savedSchedule);
        scheduleDatesExcludedRepository.saveAll(excludedDates);

        var legs = buildRouteWeekdayLegs(savedSchedule, buses, ROUTE1_WAVES);
        saveLegs(legs);
        log.info("Route 1 Weekday legs saved: {}", legs.size());
    }

    // ============================================
    // ROUTE 2: SWC ↔ APK ↔ APB (5 buses)
    // ============================================
    private void createRoute2Schedule(List<Bus> buses) {
        log.info("Creating Route 2 Schedule (SWC ↔ APK ↔ APB) - {} buses", buses.size());

        var schedule = Schedule.builder()
                .route(Route.ROUTE_2)
                .validFromDate(YEAR_START)
                .validToDate(YEAR_END)
                .build();
        var savedSchedule = scheduleRepository.save(schedule);

        var excludedDates = createExcludedDatesForYear(savedSchedule);
        scheduleDatesExcludedRepository.saveAll(excludedDates);

        var legs = buildRouteWeekdayLegs(savedSchedule, buses, ROUTE2_WAVES);
        saveLegs(legs);
        log.info("Route 2 Weekday legs saved: {}", legs.size());
    }

    // ============================================
    // ROUTE 3: SWC ↔ DFC (4 buses)
    // ============================================
    private void createRoute3Schedule(List<Bus> buses) {
        log.info("Creating Route 3 Schedule (SWC ↔ DFC) - {} buses", buses.size());

        var schedule = Schedule.builder()
                .route(Route.ROUTE_3)
                .validFromDate(YEAR_START)
                .validToDate(YEAR_END)
                .build();
        var savedSchedule = scheduleRepository.save(schedule);

        var excludedDates = createExcludedDatesForYear(savedSchedule);
        scheduleDatesExcludedRepository.saveAll(excludedDates);

        var legs = buildRouteWeekdayLegs(savedSchedule, buses, ROUTE3_WAVES);
        saveLegs(legs);
        log.info("Route 3 Weekday legs saved: {}", legs.size());
    }

    // ============================================
    // ROUTE JBS: APK ↔ APB ↔ JBS (2 buses)
    // ============================================
    private void createRouteJBSSchedule(List<Bus> buses) {
        log.info("Creating Route JBS Schedule (APK ↔ APB ↔ JBS) - {} buses", buses.size());

        var schedule = Schedule.builder()
                .route(Route.ROUTE_JBS)
                .validFromDate(YEAR_START)
                .validToDate(YEAR_END)
                .build();
        var savedSchedule = scheduleRepository.save(schedule);

        var excludedDates = createExcludedDatesForYear(savedSchedule);
        scheduleDatesExcludedRepository.saveAll(excludedDates);

        var legs = buildRouteWeekdayLegs(savedSchedule, buses, ROUTE_JBS_WAVES);
        saveLegs(legs);
        log.info("Route JBS Weekday legs saved: {}", legs.size());
    }

    // ============================================
    // ROUTE JBS SATURDAY: ONLY JBS RUNS
    // ============================================
    private void createRouteJBSSaturdaySchedule(List<Bus> buses) {
        log.info("Creating Route JBS Saturday Schedule - {} buses", buses.size());

        var schedule = Schedule.builder()
                .route(Route.ROUTE_JBS)
                .validFromDate(YEAR_START)
                .validToDate(YEAR_END)
                .build();
        var savedSchedule = scheduleRepository.save(schedule);

        var legs = buildRouteSaturdayLegs(savedSchedule, buses, ROUTE_JBS_SATURDAY_WAVES);
        saveLegs(legs);
        log.info("Route JBS Saturday legs saved: {}", legs.size());
    }

    // ============================================
    // HELPER: Build Weekday Legs
    // ============================================
    private List<ScheduleLeg> buildRouteWeekdayLegs(Schedule schedule, List<Bus> buses, List<RouteWave> waves) {
        var legs = new ArrayList<ScheduleLeg>();
        int waveCount = waves.size();

        for (DayOfWeek dow : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            for (int i = 0; i < waveCount; i++) {
                Bus bus = buses.get(i % buses.size());
                legs.addAll(createLegsFromWaves(schedule, bus, List.of(waves.get(i)), dow));
            }
        }
        return legs;
    }

    // ============================================
    // HELPER: Build Saturday Legs
    // ============================================
    private List<ScheduleLeg> buildRouteSaturdayLegs(Schedule schedule, List<Bus> buses, List<RouteWave> waves) {
        var legs = new ArrayList<ScheduleLeg>();
        var saturday = DayOfWeek.SATURDAY;

        for (int i = 0; i < waves.size(); i++) {
            Bus bus = buses.get(i % buses.size());
            legs.addAll(createLegsFromWaves(schedule, bus, List.of(waves.get(i)), saturday));
        }
        return legs;
    }

    // ============================================
    // CORE HELPER: Create Legs from Waves
    // ============================================
    private List<ScheduleLeg> createLegsFromWaves(Schedule schedule, Bus bus, List<RouteWave> waves, DayOfWeek dayOfWeek) {
        var legs = new ArrayList<ScheduleLeg>();

        for (RouteWave wave : waves) {
            LocalTime currentTime = wave.startTime;

            for (LegSegment segment : wave.segments) {
                LocalTime departureTime = currentTime;
                LocalTime arrivalTime = currentTime.plusMinutes(segment.durationMinutes);

                var leg = ScheduleLeg.builder()
                        .schedule(schedule)
                        .fromDestination(segment.from)
                        .toDestination(segment.to)
                        .departureTime(departureTime)
                        .arrivalTime(arrivalTime)
                        .dayOfWeek(dayOfWeek)
                        .build();

                var assignment = ScheduleLegBusAssignment.builder()
                        .bus(bus)
                        .isCompleted(false)
                        .scheduleLeg(leg)
                        .build();

                if (leg.getBusesAssigned() == null) {
                    leg.setBusesAssigned(new ArrayList<>());
                }
                leg.getBusesAssigned().add(assignment);
                legs.add(leg);

                currentTime = arrivalTime;
            }
        }
        return legs;
    }

    // ============================================
    // SAVE LEGS HELPER
    // ============================================
    private void saveLegs(List<ScheduleLeg> legs) {
        for (var leg : legs) {
            var savedLeg = scheduleLegRepository.save(leg);
            for (var assignment : savedLeg.getBusesAssigned()) {
                assignment.setScheduleLeg(savedLeg);
                scheduleLegBusAssignmentRepository.save(assignment);
            }
        }
    }

    // ============================================
    // EXCLUDED DATES
    // ============================================
    private List<ScheduleDatesExcluded> createExcludedDatesForYear(Schedule schedule) {
        var excluded = new ArrayList<ScheduleDatesExcluded>();

        var sundays = getSundaysInRange(YEAR_START, YEAR_END);
        for (var sunday : sundays) {
            excluded.add(createExcludedDate(schedule, sunday, sunday, DaysExcludedReason.SUNDAY));
        }

        // Public Holidays
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 3, 21), LocalDate.of(2026, 3, 21), DaysExcludedReason.PUBLIC_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 6), DaysExcludedReason.PUBLIC_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 4, 27), LocalDate.of(2026, 4, 27), DaysExcludedReason.PUBLIC_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 1), DaysExcludedReason.PUBLIC_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 5, 25), LocalDate.of(2026, 5, 25), DaysExcludedReason.PUBLIC_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 6, 16), LocalDate.of(2026, 6, 16), DaysExcludedReason.PUBLIC_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 9, 24), LocalDate.of(2026, 9, 24), DaysExcludedReason.PUBLIC_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 12, 16), LocalDate.of(2026, 12, 16), DaysExcludedReason.PUBLIC_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 25), DaysExcludedReason.PUBLIC_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 12, 26), LocalDate.of(2026, 12, 26), DaysExcludedReason.PUBLIC_HOLIDAYS));

        // Recess holidays
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 3, 28), LocalDate.of(2026, 4, 5), DaysExcludedReason.RECESS_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 5, 17), LocalDate.of(2026, 5, 24), DaysExcludedReason.RECESS_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 6, 20), LocalDate.of(2026, 7, 5), DaysExcludedReason.RECESS_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 7, 27), LocalDate.of(2026, 7, 31), DaysExcludedReason.RECESS_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 8, 29), LocalDate.of(2026, 9, 6), DaysExcludedReason.RECESS_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 9, 17), LocalDate.of(2026, 9, 21), DaysExcludedReason.RECESS_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 10, 17), LocalDate.of(2026, 10, 22), DaysExcludedReason.RECESS_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22), DaysExcludedReason.RECESS_HOLIDAYS));
        excluded.add(createExcludedDate(schedule, LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 27), DaysExcludedReason.RECESS_HOLIDAYS));

        return excluded;
    }

    private List<LocalDate> getSundaysInRange(LocalDate start, LocalDate end) {
        var sundays = new ArrayList<LocalDate>();
        var date = start;
        while (!date.isAfter(end)) {
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                sundays.add(date);
            }
            date = date.plusDays(1);
        }
        return sundays;
    }

    private ScheduleDatesExcluded createExcludedDate(Schedule schedule, LocalDate from, LocalDate to, DaysExcludedReason reason) {
        return ScheduleDatesExcluded.builder()
                .schedule(schedule)
                .fromDate(from)
                .toDate(to)
                .reason(reason)
                .build();
    }

    // ============================================
    // INNER CLASSES
    // ============================================
    @AllArgsConstructor
    private static class RouteWave {
        LocalTime startTime;
        List<LegSegment> segments;
    }

    @AllArgsConstructor
    private static class LegSegment {
        Destination from;
        Destination to;
        int durationMinutes;
    }
}