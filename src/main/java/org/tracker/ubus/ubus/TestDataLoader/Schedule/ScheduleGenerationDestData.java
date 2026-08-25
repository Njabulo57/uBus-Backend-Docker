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

    private static final LocalDate YEAR_START = LocalDate.of(2026, 1, 1);
    private static final LocalDate YEAR_END = LocalDate.of(2026, 12, 31);

    private static final List<DayOfWeek> WEEKDAYS = List.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    );

    // ============================================
    // ROUTE 1 WAVES — DFC-APK
    // ============================================
    private static final List<RouteWave> ROUTE1_WAVES = List.of(
            // ===== Morning =====
            new RouteWave(LocalTime.of(6, 20), commonLegs()),
            new RouteWave(LocalTime.of(6, 30), commonLegs()),
            new RouteWave(LocalTime.of(6, 40), commonLegs()),
            new RouteWave(LocalTime.of(6, 50), commonLegs()),
            new RouteWave(LocalTime.of(7, 50), commonLegs()),
            new RouteWave(LocalTime.of(8, 0),  commonLegs()),
            new RouteWave(LocalTime.of(8, 20), commonLegs()),
            new RouteWave(LocalTime.of(8, 40), commonLegs()),
            new RouteWave(LocalTime.of(9, 0),  commonLegs()),
            new RouteWave(LocalTime.of(9, 15), commonLegs()),
            new RouteWave(LocalTime.of(9, 30), commonLegs()),
            new RouteWave(LocalTime.of(10, 5), commonLegs()),
            new RouteWave(LocalTime.of(10, 45), commonLegs()),
            new RouteWave(LocalTime.of(11, 35), commonLegs()),
            new RouteWave(LocalTime.of(11, 40), commonLegs()),
            new RouteWave(LocalTime.of(11, 45), commonLegs()),
            new RouteWave(LocalTime.of(11, 50), commonLegs()),
            new RouteWave(LocalTime.of(12, 0),  commonLegs()),
            new RouteWave(LocalTime.of(12, 10), commonLegs()),
            new RouteWave(LocalTime.of(12, 20), commonLegs()),
            new RouteWave(LocalTime.of(12, 30), commonLegs()),
            new RouteWave(LocalTime.of(13, 15), commonLegs()),
            new RouteWave(LocalTime.of(13, 20), commonLegs()),
            new RouteWave(LocalTime.of(13, 25), commonLegs()),
            new RouteWave(LocalTime.of(13, 35), commonLegs()),
            new RouteWave(LocalTime.of(13, 40), commonLegs()),
            new RouteWave(LocalTime.of(13, 50), commonLegs()),
            new RouteWave(LocalTime.of(14, 10), commonLegs()),
            new RouteWave(LocalTime.of(14, 30), commonLegs()),
            new RouteWave(LocalTime.of(14, 55), commonLegs()),
            new RouteWave(LocalTime.of(15, 0),  commonLegs()),
            new RouteWave(LocalTime.of(15, 5),  commonLegs()),
            new RouteWave(LocalTime.of(15, 15), commonLegs()),
            new RouteWave(LocalTime.of(15, 30), commonLegs()),
            new RouteWave(LocalTime.of(15, 50), commonLegs()),
            new RouteWave(LocalTime.of(16, 35), commonLegs()),
            new RouteWave(LocalTime.of(17, 5),  commonLegs()),
            new RouteWave(LocalTime.of(17, 10), commonLegs()),
            new RouteWave(LocalTime.of(17, 30), commonLegs()),
            new RouteWave(LocalTime.of(18, 30), commonLegs()),
            new RouteWave(LocalTime.of(19, 0),  commonLegs()),
            new RouteWave(LocalTime.of(19, 30), commonLegs()),
            new RouteWave(LocalTime.of(20, 0),  commonLegs()),
            new RouteWave(LocalTime.of(20, 30), commonLegs()),
            new RouteWave(LocalTime.of(21, 0),  commonLegs()),
            new RouteWave(LocalTime.of(21, 30), commonLegs()),
            new RouteWave(LocalTime.of(22, 10), commonLegs())
    );

    private static List<LegSegment> commonLegs() {
        return List.of(
                new LegSegment(Destination.DFC, Destination.APB, 30),
                new LegSegment(Destination.APB, Destination.APK, 10),
                new LegSegment(Destination.APK, Destination.APK, 5),
                new LegSegment(Destination.APK, Destination.APB, 10),
                new LegSegment(Destination.APB, Destination.DFC, 30)
        );
    }

    // ============================================
    // ROUTE 2 WAVES — SWC-APK-APB
    // ============================================
    private static final List<RouteWave> ROUTE2_WAVES = List.of(
            new RouteWave(LocalTime.of(6, 10), route2Legs()),
            new RouteWave(LocalTime.of(6, 30), route2Legs()),
            new RouteWave(LocalTime.of(6, 50), route2Legs()),
            new RouteWave(LocalTime.of(7, 10), route2Legs()),
            new RouteWave(LocalTime.of(7, 30), route2Legs()),
            new RouteWave(LocalTime.of(7, 40), route2Legs()),
            new RouteWave(LocalTime.of(7, 50), route2Legs()),
            new RouteWave(LocalTime.of(8, 0),  route2Legs()),
            new RouteWave(LocalTime.of(8, 15), route2Legs()),
            new RouteWave(LocalTime.of(8, 35), route2Legs()),
            new RouteWave(LocalTime.of(8, 55), route2Legs()),
            new RouteWave(LocalTime.of(9, 15), route2Legs()),
            new RouteWave(LocalTime.of(9, 25), route2Legs()),
            new RouteWave(LocalTime.of(9, 35), route2Legs()),
            new RouteWave(LocalTime.of(9, 45), route2Legs()),
            new RouteWave(LocalTime.of(10, 0), route2Legs()),
            new RouteWave(LocalTime.of(10, 15), route2Legs()),
            new RouteWave(LocalTime.of(10, 35), route2Legs()),
            new RouteWave(LocalTime.of(11, 20), route2Legs()),
            new RouteWave(LocalTime.of(11, 30), route2Legs()),
            new RouteWave(LocalTime.of(11, 50), route2Legs()),
            new RouteWave(LocalTime.of(12, 0), route2Legs()),
            new RouteWave(LocalTime.of(12, 10), route2Legs()),
            new RouteWave(LocalTime.of(12, 25), route2Legs()),
            new RouteWave(LocalTime.of(12, 40), route2Legs()),
            new RouteWave(LocalTime.of(13, 10), route2Legs()),
            new RouteWave(LocalTime.of(13, 20), route2Legs()),
            new RouteWave(LocalTime.of(13, 40), route2Legs()),
            new RouteWave(LocalTime.of(13, 50), route2Legs()),
            new RouteWave(LocalTime.of(14, 0), route2Legs()),
            new RouteWave(LocalTime.of(14, 10), route2Legs()),
            new RouteWave(LocalTime.of(14, 30), route2Legs()),
            new RouteWave(LocalTime.of(15, 0), route2Legs()),
            new RouteWave(LocalTime.of(15, 15), route2Legs()),
            new RouteWave(LocalTime.of(15, 40), route2Legs()),
            new RouteWave(LocalTime.of(16, 0), route2Legs()),
            new RouteWave(LocalTime.of(16, 20), route2Legs()),
            new RouteWave(LocalTime.of(16, 30), route2Legs()),
            new RouteWave(LocalTime.of(17, 0), route2Legs()),
            new RouteWave(LocalTime.of(17, 10), route2Legs()),
            new RouteWave(LocalTime.of(17, 45), route2Legs()),
            new RouteWave(LocalTime.of(18, 10), route2Legs()),
            new RouteWave(LocalTime.of(18, 45), route2Legs()),
            new RouteWave(LocalTime.of(20, 0), route2Legs()),
            new RouteWave(LocalTime.of(20, 30), route2Legs()),
            new RouteWave(LocalTime.of(21, 0), route2Legs()),
            new RouteWave(LocalTime.of(21, 30), route2Legs())
    );

    private static List<LegSegment> route2Legs() {
        return List.of(
                new LegSegment(Destination.SWC, Destination.APK, 40),
                new LegSegment(Destination.APK, Destination.APK, 5),
                new LegSegment(Destination.APK, Destination.APB, 15),
                new LegSegment(Destination.APB, Destination.SWC, 40)
        );
    }

    // ============================================
    // ROUTE 3 WAVES — SWC-DFC (Direct)
    // ============================================
    private static final List<RouteWave> ROUTE3_WAVES = List.of(
            new RouteWave(LocalTime.of(6, 20),
                    List.of(
                            new LegSegment(Destination.SWC, Destination.DFC, 45),
                            new LegSegment(Destination.DFC, Destination.SWC, 35)
                    )),
            new RouteWave(LocalTime.of(7, 45),
                    List.of(
                            new LegSegment(Destination.SWC, Destination.DFC, 40),
                            new LegSegment(Destination.DFC, Destination.SWC, 40)
                    )),
            new RouteWave(LocalTime.of(9, 10),
                    List.of(
                            new LegSegment(Destination.SWC, Destination.DFC, 40),
                            new LegSegment(Destination.DFC, Destination.SWC, 40)
                    )),
            new RouteWave(LocalTime.of(10, 35),
                    List.of(
                            new LegSegment(Destination.SWC, Destination.DFC, 40),
                            new LegSegment(Destination.DFC, Destination.SWC, 40)
                    )),
            new RouteWave(LocalTime.of(12, 5),
                    List.of(
                            new LegSegment(Destination.SWC, Destination.DFC, 40),
                            new LegSegment(Destination.DFC, Destination.SWC, 40)
                    )),
            new RouteWave(LocalTime.of(13, 35),
                    List.of(
                            new LegSegment(Destination.SWC, Destination.DFC, 40),
                            new LegSegment(Destination.DFC, Destination.SWC, 40)
                    )),
            new RouteWave(LocalTime.of(15, 5),
                    List.of(
                            new LegSegment(Destination.SWC, Destination.DFC, 40),
                            new LegSegment(Destination.DFC, Destination.SWC, 40)
                    )),
            new RouteWave(LocalTime.of(16, 30),
                    List.of(
                            new LegSegment(Destination.SWC, Destination.DFC, 40),
                            new LegSegment(Destination.DFC, Destination.SWC, 40)
                    )),
            new RouteWave(LocalTime.of(18, 0),
                    List.of(
                            new LegSegment(Destination.SWC, Destination.DFC, 35),
                            new LegSegment(Destination.DFC, Destination.SWC, 45)
                    )),
            new RouteWave(LocalTime.of(19, 25),
                    List.of(
                            new LegSegment(Destination.SWC, Destination.DFC, 40),
                            new LegSegment(Destination.DFC, Destination.SWC, 40)
                    )),
            new RouteWave(LocalTime.of(20, 55),
                    List.of(
                            new LegSegment(Destination.SWC, Destination.DFC, 40),
                            new LegSegment(Destination.DFC, Destination.SWC, 40)
                    ))
    );

    // ============================================
    // ROUTE JBS WAVES — APK-APB-JBS
    // ============================================
    private static final List<RouteWave> ROUTE_JBS_WAVES = List.of(
            new RouteWave(LocalTime.of(7, 0),
                    List.of(
                            new LegSegment(Destination.APK, Destination.APB, 10),
                            new LegSegment(Destination.APB, Destination.JBS, 10)
                    )),
            new RouteWave(LocalTime.of(9, 0),
                    List.of(
                            new LegSegment(Destination.APK, Destination.APB, 10),
                            new LegSegment(Destination.APB, Destination.JBS, 10)
                    )),
            new RouteWave(LocalTime.of(17, 0),
                    List.of(
                            new LegSegment(Destination.APK, Destination.APB, 10),
                            new LegSegment(Destination.APB, Destination.JBS, 10)
                    )),
            new RouteWave(LocalTime.of(19, 0),
                    List.of(
                            new LegSegment(Destination.APK, Destination.APB, 10),
                            new LegSegment(Destination.APB, Destination.JBS, 10)
                    )),
            new RouteWave(LocalTime.of(21, 10),
                    List.of(
                            new LegSegment(Destination.JBS, Destination.APB, 10),
                            new LegSegment(Destination.APB, Destination.APK, 10)
                    ))
    );

    // ============================================
    // SATURDAY WAVES
    // ============================================
    private static final List<RouteWave> ROUTE1_SATURDAY_WAVES = List.of(
            new RouteWave(LocalTime.of(7, 0), commonLegs()),
            new RouteWave(LocalTime.of(9, 0), commonLegs()),
            new RouteWave(LocalTime.of(11, 0), commonLegs())
    );

    private static final List<RouteWave> ROUTE2_SATURDAY_WAVES = List.of(
            new RouteWave(LocalTime.of(7, 0), route2Legs()),
            new RouteWave(LocalTime.of(9, 0), route2Legs())
    );

    private static final List<RouteWave> ROUTE3_SATURDAY_WAVES = List.of(
            new RouteWave(LocalTime.of(7, 0),
                    List.of(
                            new LegSegment(Destination.SWC, Destination.DFC, 40),
                            new LegSegment(Destination.DFC, Destination.SWC, 40)
                    )),
            new RouteWave(LocalTime.of(8, 0),
                    List.of(
                            new LegSegment(Destination.DFC, Destination.SWC, 40),
                            new LegSegment(Destination.SWC, Destination.DFC, 40)
                    ))
    );

    private static final List<RouteWave> ROUTE_JBS_SATURDAY_WAVES = List.of(
            new RouteWave(LocalTime.of(8, 0),
                    List.of(
                            new LegSegment(Destination.APK, Destination.APB, 10),
                            new LegSegment(Destination.APB, Destination.JBS, 10),
                            new LegSegment(Destination.JBS, Destination.APB, 30),
                            new LegSegment(Destination.APB, Destination.APK, 10)
                    )),
            new RouteWave(LocalTime.of(12, 0),
                    List.of(
                            new LegSegment(Destination.APK, Destination.APB, 10),
                            new LegSegment(Destination.APB, Destination.JBS, 10),
                            new LegSegment(Destination.JBS, Destination.APB, 30),
                            new LegSegment(Destination.APB, Destination.APK, 10)
                    ))
    );

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== Populating Schedule Data for Full Year 2026 ===");
        if (scheduleRepository.count() > 0) {
            log.info("✅ Schedules already exist. Skipping generation.");
            return;
        }

        var allOperationalBuses = busRepository.findByOperationalStatusAndIsActiveTrue(BusOperationalStatus.OPERATIONAL);
        if (allOperationalBuses.isEmpty()) {
            log.warn("No operational buses found.");
            return;
        }
        log.info("Found {} operational buses", allOperationalBuses.size());

        // Categorize buses by prefix
        var dfcBuses = allOperationalBuses.stream()
                .filter(bus -> bus.getName() != null && bus.getName().startsWith("DFC"))
                .sorted(Comparator.comparing(Bus::getName))
                .collect(Collectors.toList());

        var swcBuses = allOperationalBuses.stream()
                .filter(bus -> bus.getName() != null && bus.getName().startsWith("SWC"))
                .sorted(Comparator.comparing(Bus::getName))
                .collect(Collectors.toList());

        var jbsBuses = allOperationalBuses.stream()
                .filter(bus -> bus.getName() != null && bus.getName().startsWith("JBS"))
                .sorted(Comparator.comparing(Bus::getName))
                .collect(Collectors.toList());

        log.info("DFC buses: {}", dfcBuses.size());
        log.info("SWC buses: {}", swcBuses.size());
        log.info("JBS buses: {}", jbsBuses.size());

        createRoute1Schedule(dfcBuses);
        createRoute2Schedule(swcBuses);
        createRoute3Schedule(swcBuses, dfcBuses);
        createRouteJBSSchedule(jbsBuses);

        log.info("=== Schedule Data Population Complete ===");
    }

    // ============================================
    // ROUTE 1: DFC ↔ APB ↔ APK (ONLY DFC buses)
    // ============================================
    private void createRoute1Schedule(List<Bus> dfcBuses) {
        log.info("Creating Route 1 Schedule (DFC ↔ APB ↔ APK) - Full Year");

        if (dfcBuses.size() < 7) {
            log.warn("Not enough DFC buses. Found: {}, Need: 7", dfcBuses.size());
            return;
        }

        var schedule = Schedule.builder()
                .route(Route.ROUTE_1)
                .validFromDate(YEAR_START)
                .validToDate(YEAR_END)
                .build();
        var savedSchedule = scheduleRepository.save(schedule);

        var excludedDates = createExcludedDatesForYear(savedSchedule);
        scheduleDatesExcludedRepository.saveAll(excludedDates);

        var weekdayLegs = buildRoute1WeekdayLegs(savedSchedule, dfcBuses);
        saveLegs(weekdayLegs);
        log.info("Route 1 Weekday legs saved: {}", weekdayLegs.size());

        var saturdayLegs = buildRoute1SaturdayLegs(savedSchedule, dfcBuses);
        saveLegs(saturdayLegs);
        log.info("Route 1 Saturday legs saved: {}", saturdayLegs.size());
    }

    private List<ScheduleLeg> buildRoute1WeekdayLegs(Schedule schedule, List<Bus> dfcBuses) {
        var legs = new ArrayList<ScheduleLeg>();

        for (DayOfWeek dow : WEEKDAYS) {
            for (int i = 0; i < ROUTE1_WAVES.size(); i++) {
                Bus bus = dfcBuses.get(i % dfcBuses.size());
                legs.addAll(createLegsFromWaves(schedule, bus, List.of(ROUTE1_WAVES.get(i)), dow));
            }
        }
        return legs;
    }

    private List<ScheduleLeg> buildRoute1SaturdayLegs(Schedule schedule, List<Bus> dfcBuses) {
        var legs = new ArrayList<ScheduleLeg>();
        var saturday = DayOfWeek.SATURDAY;

        // Use first three buses for Saturday service (as per existing logic)
        legs.addAll(createLegsFromWaves(schedule, dfcBuses.get(0), ROUTE1_SATURDAY_WAVES, saturday));
        legs.addAll(createLegsFromWaves(schedule, dfcBuses.get(1), ROUTE1_SATURDAY_WAVES, saturday));
        legs.addAll(createLegsFromWaves(schedule, dfcBuses.get(2), ROUTE1_SATURDAY_WAVES, saturday));

        return legs;
    }

    // ============================================
    // ROUTE 2: SWC ↔ APK ↔ APB (ONLY SWC buses)
    // ============================================
    private void createRoute2Schedule(List<Bus> swcBuses) {
        log.info("Creating Route 2 Schedule (SWC ↔ APK ↔ APB) - Full Year");

        if (swcBuses.size() < 5) {
            log.warn("Not enough SWC buses. Found: {}, Need: 5", swcBuses.size());
            return;
        }

        var schedule = Schedule.builder()
                .route(Route.ROUTE_2)
                .validFromDate(YEAR_START)
                .validToDate(YEAR_END)
                .build();
        var savedSchedule = scheduleRepository.save(schedule);

        var excludedDates = createExcludedDatesForYear(savedSchedule);
        scheduleDatesExcludedRepository.saveAll(excludedDates);

        var weekdayLegs = buildRoute2WeekdayLegs(savedSchedule, swcBuses);
        saveLegs(weekdayLegs);
        log.info("Route 2 Weekday legs saved: {}", weekdayLegs.size());

        var saturdayLegs = buildRoute2SaturdayLegs(savedSchedule, swcBuses);
        saveLegs(saturdayLegs);
        log.info("Route 2 Saturday legs saved: {}", saturdayLegs.size());
    }

    private List<ScheduleLeg> buildRoute2WeekdayLegs(Schedule schedule, List<Bus> swcBuses) {
        var legs = new ArrayList<ScheduleLeg>();

        for (DayOfWeek dow : WEEKDAYS) {
            for (int i = 0; i < ROUTE2_WAVES.size(); i++) {
                Bus bus = swcBuses.get(i % swcBuses.size());
                legs.addAll(createLegsFromWaves(schedule, bus, List.of(ROUTE2_WAVES.get(i)), dow));
            }
        }
        return legs;
    }

    private List<ScheduleLeg> buildRoute2SaturdayLegs(Schedule schedule, List<Bus> swcBuses) {
        var legs = new ArrayList<ScheduleLeg>();
        var saturday = DayOfWeek.SATURDAY;

        legs.addAll(createLegsFromWaves(schedule, swcBuses.get(0), ROUTE2_SATURDAY_WAVES, saturday));
        legs.addAll(createLegsFromWaves(schedule, swcBuses.get(1), ROUTE2_SATURDAY_WAVES, saturday));

        return legs;
    }

    // ============================================
    // ROUTE 3: SWC ↔ DFC (SWC and DFC buses only)
    // ============================================
    private void createRoute3Schedule(List<Bus> swcBuses, List<Bus> dfcBuses) {
        log.info("Creating Route 3 Schedule (SWC ↔ DFC) - Full Year");

        if (swcBuses.size() < 2 || dfcBuses.isEmpty()) {
            log.warn("Not enough buses for Route 3. SWC: {}, DFC: {}", swcBuses.size(), dfcBuses.size());
            return;
        }

        var schedule = Schedule.builder()
                .route(Route.ROUTE_3)
                .validFromDate(YEAR_START)
                .validToDate(YEAR_END)
                .build();
        var savedSchedule = scheduleRepository.save(schedule);

        var excludedDates = createExcludedDatesForYear(savedSchedule);
        scheduleDatesExcludedRepository.saveAll(excludedDates);

        var weekdayLegs = buildRoute3WeekdayLegs(savedSchedule, swcBuses, dfcBuses);
        saveLegs(weekdayLegs);
        log.info("Route 3 Weekday legs saved: {}", weekdayLegs.size());

        var saturdayLegs = buildRoute3SaturdayLegs(savedSchedule, swcBuses, dfcBuses);
        saveLegs(saturdayLegs);
        log.info("Route 3 Saturday legs saved: {}", saturdayLegs.size());
    }

    private List<ScheduleLeg> buildRoute3WeekdayLegs(Schedule schedule, List<Bus> swcBuses, List<Bus> dfcBuses) {
        var legs = new ArrayList<ScheduleLeg>();

        for (DayOfWeek dow : WEEKDAYS) {
            legs.addAll(createLegsFromWaves(schedule, swcBuses.get(0), ROUTE3_WAVES, dow));
            legs.addAll(createLegsFromWaves(schedule, dfcBuses.get(0), ROUTE3_WAVES, dow));
            legs.addAll(createLegsFromWaves(schedule, swcBuses.get(1), ROUTE3_WAVES, dow));
        }
        return legs;
    }

    private List<ScheduleLeg> buildRoute3SaturdayLegs(Schedule schedule, List<Bus> swcBuses, List<Bus> dfcBuses) {
        var legs = new ArrayList<ScheduleLeg>();
        var saturday = DayOfWeek.SATURDAY;

        legs.addAll(createLegsFromWaves(schedule, swcBuses.get(0), ROUTE3_SATURDAY_WAVES, saturday));
        legs.addAll(createLegsFromWaves(schedule, dfcBuses.get(0), ROUTE3_SATURDAY_WAVES, saturday));

        return legs;
    }

    // ============================================
    // ROUTE JBS: APK ↔ APB ↔ JBS (ONLY JBS buses)
    // ============================================
    private void createRouteJBSSchedule(List<Bus> jbsBuses) {
        log.info("Creating Route JBS Schedule (APK ↔ APB ↔ JBS) - Full Year");

        if (jbsBuses.size() < 2) {
            log.warn("Not enough JBS buses. Found: {}", jbsBuses.size());
            return;
        }

        var schedule = Schedule.builder()
                .route(Route.ROUTE_JBS)
                .validFromDate(YEAR_START)
                .validToDate(YEAR_END)
                .build();
        var savedSchedule = scheduleRepository.save(schedule);

        var excludedDates = createExcludedDatesForYear(savedSchedule);
        scheduleDatesExcludedRepository.saveAll(excludedDates);

        var weekdayLegs = buildRouteJBSWeekdayLegs(savedSchedule, jbsBuses);
        saveLegs(weekdayLegs);
        log.info("Route JBS Weekday legs saved: {}", weekdayLegs.size());

        var saturdayLegs = buildRouteJBSSaturdayLegs(savedSchedule, jbsBuses);
        saveLegs(saturdayLegs);
        log.info("Route JBS Saturday legs saved: {}", saturdayLegs.size());
    }

    private List<ScheduleLeg> buildRouteJBSWeekdayLegs(Schedule schedule, List<Bus> jbsBuses) {
        var legs = new ArrayList<ScheduleLeg>();

        for (DayOfWeek dow : WEEKDAYS) {
            // Bus 1 handles the first three outbound trips and the return
            legs.addAll(createLegsFromWaves(schedule, jbsBuses.get(0),
                    List.of(ROUTE_JBS_WAVES.get(0), ROUTE_JBS_WAVES.get(1), ROUTE_JBS_WAVES.get(4)), dow));
            // Bus 2 handles the remaining two outbound trips
            legs.addAll(createLegsFromWaves(schedule, jbsBuses.get(1),
                    List.of(ROUTE_JBS_WAVES.get(2), ROUTE_JBS_WAVES.get(3)), dow));
        }
        return legs;
    }

    private List<ScheduleLeg> buildRouteJBSSaturdayLegs(Schedule schedule, List<Bus> jbsBuses) {
        var legs = new ArrayList<ScheduleLeg>();
        var saturday = DayOfWeek.SATURDAY;

        legs.addAll(createLegsFromWaves(schedule, jbsBuses.get(0), ROUTE_JBS_SATURDAY_WAVES, saturday));
        legs.addAll(createLegsFromWaves(schedule, jbsBuses.get(1), ROUTE_JBS_SATURDAY_WAVES, saturday));

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

                // Ensure the collection is not null
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

        var sundays = getSundaysInYear(2026);
        for (var sunday : sundays) {
            excluded.add(createExcludedDate(schedule, sunday, sunday, DaysExcludedReason.SUNDAY));
        }

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

    private List<LocalDate> getSundaysInYear(int year) {
        var sundays = new ArrayList<LocalDate>();
        var date = LocalDate.of(year, 1, 1);
        while (date.getYear() == year) {
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