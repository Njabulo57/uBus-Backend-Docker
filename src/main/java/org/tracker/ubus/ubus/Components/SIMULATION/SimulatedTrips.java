package org.tracker.ubus.ubus.Components.SIMULATION;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusType;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Enum.DriverSchedule;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLeg;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLegBusAssignment;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusOperationalStatus.OPERATIONAL;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class SimulatedTrips {

    private static final int TOTAL_DRIVERS_PER_SHIFT = 8;
    private static final int[] TOTAL_CAPACITIES = new int[]{50, 40, 65, 30, 55, 70, 45};
    private static final String[] BUS_MODEL_NAMES = new String[]{"Volvo", "BMW", "Toyota", "Ford", "Man", "Audi"};
    private static final String[] NAME_PREFIXES = new String[]{"DFC-APK", "SWC-APB", "APK-JBS", "SWC-DFC"};

    // Route trip counts
    private static final Map<String, Integer> ROUTE_TRIP_COUNTS = Map.of(
            "DFC-APK", 3,  // Route 1 - 3 trips
            "SWC-APB", 3,  // Route 2 - 3 trips
            "APK-JBS", 3,  // JBS - 3 trips
            "SWC-DFC", 4   // SWC - 4 trips
    );

    private final Collection<Trip> simulationTrips = new ArrayList<>();
    private final Map<Route, Schedule> routeScheduleMap = new ConcurrentHashMap<>();
    private final Map<Route, List<ScheduleLeg>> routeLegsMap = new ConcurrentHashMap<>();
    private final Map<Route, List<ScheduleLegBusAssignment>> routeAssignmentsMap = new ConcurrentHashMap<>();

    private final SecureRandom random = new SecureRandom();
    private final Faker faker = new Faker();

    @PostConstruct
    private void initTrips() {

        var validRoutes = Route.ALL_ROUTES.stream()
                .filter(route -> Arrays.asList(NAME_PREFIXES).contains(route.getLabel()))
                .toList();

        var busesByRoute = getBusNamesGroupedByRoute(validRoutes);

        var morningDrivers = createDriversForShift(TOTAL_DRIVERS_PER_SHIFT);
        var afternoonDrivers = createDriversForShift(TOTAL_DRIVERS_PER_SHIFT);

        // Store assignments by route
        Map<Route, List<BusAssignment>> routeBusAssignments = new HashMap<>();

        busesByRoute.forEach((route, busNames) -> {
            String busName = busNames.iterator().next();
            var bus = createSimulationBus(busName, route);
            var morningDriver = morningDrivers.get(random.nextInt(morningDrivers.size()));
            var morningAssignment = createSimulationBusAssignment(bus, morningDriver, DriverSchedule.MORNING_AFTERNOON);
            var afternoonDriver = afternoonDrivers.get(random.nextInt(afternoonDrivers.size()));
            var afternoonAssignment = createSimulationBusAssignment(bus, afternoonDriver, DriverSchedule.AFTERNOON_EVENING);

            // Store BusAssignments for this route
            routeBusAssignments.put(route, List.of(morningAssignment, afternoonAssignment));

            // Create Schedule for the route
            var schedule = createSchedule(route);
            routeScheduleMap.put(route, schedule);

            // Generate legs for this route
            var legs = generateScheduleLegs(route, schedule);

            // VALIDATE: Check for any invalid legs
            var invalidLegs = legs.stream()
                    .filter(leg -> leg.getFromDestination().equals(leg.getToDestination()))
                    .toList();
            if (!invalidLegs.isEmpty()) {
                log.error("❌ Found invalid legs with same from/to: {}", invalidLegs);
                // Remove invalid legs
                legs.removeAll(invalidLegs);
            }

            routeLegsMap.put(route, legs);

            // Create bus assignments for legs
            var assignments = createBusAssignmentsForLegs(legs, morningAssignment, afternoonAssignment);
            routeAssignmentsMap.put(route, assignments);

            // Generate trips from legs - FIXED: Pass the BusAssignments
            var trips = generateTripsFromLegs(assignments, route, List.of(morningAssignment, afternoonAssignment));
            simulationTrips.addAll(trips);
        });

        log.info("✅ Initialized {} simulated trips across {} routes", simulationTrips.size(), routeScheduleMap.size());
    }

    private Schedule createSchedule(Route route) {
        var today = LocalDate.now();
        var validFrom = LocalDate.of(today.getYear(), 1, 1);
        var validTo = LocalDate.of(today.getYear(), 12, 31);

        return Schedule.builder()
                .route(route)
                .validFromDate(validFrom)
                .validToDate(validTo)
                .build();
    }

    private List<ScheduleLeg> generateScheduleLegs(Route route, Schedule schedule) {
        var legs = new ArrayList<ScheduleLeg>();
        var uniqueDestinations = route.getUniqueStops();
        var routeLabel = route.getLabel();

        // Get trip count for this route
        var tripCount = ROUTE_TRIP_COUNTS.getOrDefault(routeLabel, 3);

        // Get current time
        var now = LocalTime.now();
        // Round to nearest minute for cleaner display
        var baseTime = LocalTime.of(now.getHour(), now.getMinute());

        log.info("🕐 Generating {} trips for route {} starting at {}", tripCount, routeLabel, baseTime);

        // Generate trips at current time, +15 min, +30 min, etc.
        for (int i = 0; i < tripCount && i < uniqueDestinations.size() - 1; i++) {
            var departureTime = baseTime.plusMinutes((long) i * 15);
            var arrivalTime = departureTime.plusMinutes(45 + random.nextInt(20)); // 45-65 min duration

            // Ensure we don't go past midnight
            if (arrivalTime.isAfter(LocalTime.of(23, 59))) {
                arrivalTime = LocalTime.of(23, 59);
            }

            // FIX: Ensure from and to are never the same
            int fromIdx = i % (uniqueDestinations.size() - 1);
            int toIdx = fromIdx + 1;

            // Safety check - if fromIdx == toIdx (should never happen with this logic)
            // But just in case, skip this iteration
            if (fromIdx == toIdx) {
                log.warn("Skipping leg generation - from and to are the same for route {}", routeLabel);
                continue;
            }



            var leg = createScheduleLeg(
                    schedule,
                    uniqueDestinations.get(fromIdx),
                    uniqueDestinations.get(toIdx),
                    departureTime,
                    arrivalTime
            );
            legs.add(leg);

            log.info("  Trip {}: {} → {} at {} (arrives {})",
                    i + 1,
                    uniqueDestinations.get(fromIdx),
                    uniqueDestinations.get(toIdx),
                    departureTime,
                    arrivalTime);
        }

        // Mark one leg as delayed (add 15-30 minutes to departure)
        if (!legs.isEmpty() && legs.size() > 1) {
            var delayedIndex = random.nextInt(legs.size());
            var delayedLeg = legs.get(delayedIndex);
            var delayMinutes = 15 + random.nextInt(16); // 15-30 minutes
            var originalDeparture = delayedLeg.getDepartureTime();
            delayedLeg.setDepartureTime(originalDeparture.plusMinutes(delayMinutes));
            log.info("🟡 Delayed leg {}: {} -> {} (was {}, now {} +{}min)",
                    delayedIndex + 1,
                    delayedLeg.getFromDestination(),
                    delayedLeg.getToDestination(),
                    originalDeparture,
                    delayedLeg.getDepartureTime(),
                    delayMinutes);
        }

        log.info("📋 Generated {} legs for route {}", legs.size(), routeLabel);
        return legs;
    }

    private ScheduleLeg createScheduleLeg(Schedule schedule, Destination from, Destination to,
                                          LocalTime departureTime, LocalTime arrivalTime) {
        return ScheduleLeg.builder()
                .schedule(schedule)
                .fromDestination(from)
                .toDestination(to)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .build();
    }

    private List<ScheduleLegBusAssignment> createBusAssignmentsForLegs(
            List<ScheduleLeg> legs,
            BusAssignment morningAssignment,
            BusAssignment afternoonAssignment) {

        var assignments = new ArrayList<ScheduleLegBusAssignment>();

        for (int i = 0; i < legs.size(); i++) {
            var leg = legs.get(i);
            // Alternate between morning and afternoon assignments
            var busAssignment = (i % 2 == 0) ? morningAssignment : afternoonAssignment;

            var assignment = ScheduleLegBusAssignment.builder()
                    .scheduleLeg(leg)
                    .bus(busAssignment.getBus())
                    .isCompleted(false)
                    .build();

            assignments.add(assignment);
        }

        return assignments;
    }

    // FIXED: Now accepts BusAssignments parameter
    private List<Trip> generateTripsFromLegs(
            List<ScheduleLegBusAssignment> assignments,
            Route route,
            List<BusAssignment> busAssignments) {

        var trips = new ArrayList<Trip>();
        var today = LocalDate.now();

        for (int i = 0; i < assignments.size(); i++) {
            var assignment = assignments.get(i);
            var leg = assignment.getScheduleLeg();
            var bus = assignment.getBus();

            // FIX: Skip if from and to are the same
            if (leg.getFromDestination().equals(leg.getToDestination())) {
                log.warn("⏭️ Skipping trip generation - from and to are the same for route {}", route.getLabel());
                continue;
            }

            // Get the corresponding BusAssignment (alternate between morning and afternoon)
            var busAssignment = busAssignments.get(i % 2);

            // FIX: Build LocalDateTime properly from LocalDate and LocalTime
            LocalDateTime departureTime = LocalDateTime.of(today, leg.getDepartureTime());
            LocalDateTime arrivalTime = LocalDateTime.of(today, leg.getArrivalTime());

            // If arrival is before departure (next day), add a day
            if (arrivalTime.isBefore(departureTime)) {
                arrivalTime = arrivalTime.plusDays(1);
            }

            var trip = Trip.builder()
                    .id(UUID.randomUUID())
                    .route(route)
                    .scheduleLegBusAssignment(assignment)
                    .busAssignment(busAssignment)  // Now properly set
                    .status(null)
                    .totalCount(random.nextInt(20) + 10)
                    .departureTime(departureTime)  // Now LocalDateTime
                    .expectedArrivalTime(arrivalTime)  // Now LocalDateTime
                    .build();

            // Mark as simulation
            trip.setFromSimulation(true);

            trips.add(trip);
        }

        log.info("🚌 Generated {} trips for route {}", trips.size(), route.getLabel());
        return trips;
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private Map<Route, Collection<String>> getBusNamesGroupedByRoute(Collection<Route> validRoutes) {
        var map = new HashMap<Route, Collection<String>>();

        for (String namePrefix : NAME_PREFIXES) {
            var routeContained = validRoutes.stream()
                    .filter(route -> route.getLabel().equals(namePrefix))
                    .findFirst()
                    .orElse(null);

            if (routeContained != null) {
                var busNames = map.computeIfAbsent(routeContained, k -> new ArrayList<>());
                var nameToAdd = String.format("%s-01", namePrefix);
                busNames.add(nameToAdd);
            }
        }
        return map;
    }

    private List<User> createDriversForShift(int count) {
        var drivers = new ArrayList<User>();
        for (int i = 0; i < count; i++)
            drivers.add(createSimulationDriver());
        return drivers;
    }

    private User createSimulationDriver() {
        var name = faker.name();
        var email = faker.internet().emailAddress();
        return User.builder()
                .firstname(name.firstName())
                .lastname(name.lastName())
                .email(email)
                .build();
    }

    private BusAssignment createSimulationBusAssignment(Bus bus, User driver, DriverSchedule driverSchedule) {
        return BusAssignment.builder()
                .bus(bus)
                .driver(driver)
                .driverSchedule(driverSchedule)
                .build();
    }

    private Bus createSimulationBus(String name, Route route) {
        var busModel = generateBusModel();
        var busCapacity = generateBusCapacity();
        var busType = generateBusType();
        var registrationNumber = generateRegistrationNumber();
        return Bus.builder()
                .name(name + " SIM")
                .model(busModel)
                .capacity(busCapacity)
                .route(route)
                .registrationNumber(registrationNumber)
                .type(busType)
                .operationalStatus(OPERATIONAL)
                .activityStatus(BusActivityStatus.ON_TRIP)
                .isActive(true)
                .build();
    }

    private String generateBusModel() {
        return BUS_MODEL_NAMES[random.nextInt(BUS_MODEL_NAMES.length)];
    }

    private int generateBusCapacity() {
        return TOTAL_CAPACITIES[random.nextInt(TOTAL_CAPACITIES.length)];
    }

    private BusType generateBusType() {
        return BusType.values()[random.nextInt(BusType.values().length)];
    }

    private String generateRegistrationNumber() {
        var firstThreeDigits = String.format("%03d", random.nextInt(1000));
        var lastThreeDigits = String.format("%03d", random.nextInt(1000));
        return firstThreeDigits + " - " + lastThreeDigits;
    }


    public Collection<Trip> getSimulationTrips(LocalTime startTime, LocalTime endTime) {
        return simulationTrips.stream()
                .filter(trip -> {
                    LocalTime departureTime = trip.getDepartureTime().toLocalTime();
                    LocalTime arrivalTime = trip.getExpectedArrivalTime().toLocalTime();
                    return departureTime.isAfter(startTime) && arrivalTime.isBefore(endTime);
                })
                .collect(Collectors.toList());
    }

    public Collection<Trip> getSimulationTrips(LocalTime within) {
        return simulationTrips.stream()
                .filter(trip -> {
                    LocalTime departureTime = trip.getDepartureTime().toLocalTime();
                    LocalTime arrivalTime = trip.getExpectedArrivalTime().toLocalTime();
                    return departureTime.isBefore(within) && arrivalTime.isAfter(within);
                })
                .collect(Collectors.toList());
    }

    public Collection<Trip> getTripsForToday() {
        var today = LocalDate.now();
        return simulationTrips.stream()
                .filter(trip -> trip.getDepartureTime().toLocalDate().equals(today))
                .sorted(Comparator.comparing(Trip::getDepartureTime))
                .collect(Collectors.toList());
    }

    public Optional<Trip> getCurrentTripForRoute(Route route) {
        var now = LocalDateTime.now();
        return simulationTrips.stream()
                .filter(trip -> trip.getRoute().equals(route))
                .filter(trip -> trip.getDepartureTime().isBefore(now))
                .filter(trip -> trip.getExpectedArrivalTime().isAfter(now))
                .findFirst();
    }

    public List<Trip> getTripsForRouteToday(Route route) {
        var today = LocalDate.now();
        return simulationTrips.stream()
                .filter(trip -> trip.getRoute().equals(route))
                .filter(trip -> trip.getDepartureTime().toLocalDate().equals(today))
                .sorted(Comparator.comparing(Trip::getDepartureTime))
                .collect(Collectors.toList());
    }

    public List<Trip> getAllTrips() {
        return simulationTrips.stream()
                .filter(trip -> {
                    var to = trip.getScheduleLegBusAssignment()
                            .getScheduleLeg()
                            .getToDestination();

                    var from = trip.getScheduleLegBusAssignment()
                            .getScheduleLeg()
                            .getFromDestination();

                    return !to.equals(from);
                })
                .toList();

    }
}