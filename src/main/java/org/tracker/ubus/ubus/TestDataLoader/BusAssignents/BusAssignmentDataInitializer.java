package org.tracker.ubus.ubus.TestDataLoader.BusAssignents;

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
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3)
public class BusAssignmentDataInitializer implements CommandLineRunner {

    private final BusRepository busRepository;
    private final UserRepository userRepository;
    private final BusAssignmentRepository busAssignmentRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting bus assignment initialization...");

        // Check if bus assignments already exist
        long existingAssignments = busAssignmentRepository.count();
        if (existingAssignments > 0) {
            log.info("Found {} existing bus assignments. Skipping initialization.", existingAssignments);
            return;
        }

        // Fetch all buses
        List<Bus> buses = busRepository.findAllByIsActiveTrue();
        if (buses.isEmpty()) {
            log.warn("No buses found in database. Please ensure buses are created first.");
            return;
        }

        // Fetch all active drivers
        List<User> drivers = userRepository.findByRoleAndStatus(UserRole.DRIVER, UserStatus.ACTIVE);
        if (drivers.isEmpty()) {
            log.warn("No drivers found in database. Please ensure drivers are created first.");
            return;
        }

        log.info("Found {} buses and {} drivers", buses.size(), drivers.size());

        // Create bus assignments - 2 drivers per bus
        List<BusAssignment> assignments = createBusAssignments(buses, drivers);

        // Save all assignments
        busAssignmentRepository.saveAll(assignments);

        log.info("Successfully created {} bus assignments for {} buses", assignments.size(), buses.size());

        // Log summary
        logBusAssignmentSummary(assignments);
    }

    private List<BusAssignment> createBusAssignments(List<Bus> buses, List<User> drivers) {
        List<BusAssignment> assignments = new ArrayList<>();

        // Create a copy of drivers list to track used drivers
        List<User> availableDrivers = new ArrayList<>(drivers);
        Collections.shuffle(availableDrivers, new Random());

        int driversNeeded = buses.size() * 2; // 2 drivers per bus

        if (availableDrivers.size() < driversNeeded) {
            log.warn("Not enough drivers! Need {} drivers but only have {}. Creating assignments for available drivers.",
                    driversNeeded, availableDrivers.size());
        }

        int driverIndex = 0;

        for (Bus bus : buses) {
            // Morning shift driver
            if (driverIndex < availableDrivers.size()) {
                User morningDriver = availableDrivers.get(driverIndex++);
                BusAssignment morningAssignment = BusAssignment.builder()
                        .bus(bus)
                        .driver(morningDriver)
                        .driverSchedule(DriverSchedule.MORNING_AFTERNOON)
                        .build();
                assignments.add(morningAssignment);
                log.debug("Assigned driver {} {} to morning shift for bus {}",
                        morningDriver.getFirstname(), morningDriver.getLastname(), bus.getName());
            } else {
                log.warn("No driver available for morning shift of bus {}", bus.getName());
                break;
            }

            // Afternoon shift driver
            if (driverIndex < availableDrivers.size()) {
                User afternoonDriver = availableDrivers.get(driverIndex++);
                BusAssignment afternoonAssignment = BusAssignment.builder()
                        .bus(bus)
                        .driver(afternoonDriver)
                        .driverSchedule(DriverSchedule.AFTERNOON_EVENING)
                        .build();
                assignments.add(afternoonAssignment);
                log.debug("Assigned driver {} {} to afternoon shift for bus {}",
                        afternoonDriver.getFirstname(), afternoonDriver.getLastname(), bus.getName());
            } else {
                log.warn("No driver available for afternoon shift of bus {}", bus.getName());
                break;
            }
        }

        return assignments;
    }

    private void logBusAssignmentSummary(List<BusAssignment> assignments) {
        Map<Bus, List<BusAssignment>> assignmentsByBus = assignments.stream()
                .collect(Collectors.groupingBy(BusAssignment::getBus));

        log.info("=== Bus Assignment Summary ===");
        log.info("Total buses assigned: {}", assignmentsByBus.size());
        log.info("Total assignments created: {}", assignments.size());

        assignmentsByBus.forEach((bus, busAssignments) -> {
            log.info("Bus: {} ({}) - Route: {}",
                    bus.getName(),
                    bus.getRegistrationNumber(),
                    bus.getRoute().getLabel());

            busAssignments.forEach(assignment -> {
                log.info("  - {} shift: Driver {} {}",
                        assignment.getDriverSchedule().getLabel(),
                        assignment.getDriver().getFirstname(),
                        assignment.getDriver().getLastname());
            });
        });

        // Validate constraints
        validateAssignmentConstraints(assignments);
    }

    private void validateAssignmentConstraints(List<BusAssignment> assignments) {
        Map<Bus, List<BusAssignment>> assignmentsByBus = assignments.stream()
                .collect(Collectors.groupingBy(BusAssignment::getBus));

        boolean hasViolation = false;

        for (Map.Entry<Bus, List<BusAssignment>> entry : assignmentsByBus.entrySet()) {
            Bus bus = entry.getKey();
            List<BusAssignment> busAssignments = entry.getValue();

            // Check 1: Maximum 2 assignments per bus
            if (busAssignments.size() > 2) {
                log.error("VIOLATION: Bus {} has {} assignments (max 2 allowed)",
                        bus.getRegistrationNumber(), busAssignments.size());
                hasViolation = true;
            }

            // Check 2: No duplicate shifts for same bus
            Set<DriverSchedule> schedules = busAssignments.stream()
                    .map(BusAssignment::getDriverSchedule)
                    .collect(Collectors.toSet());

            if (schedules.size() != busAssignments.size()) {
                log.error("VIOLATION: Bus {} has duplicate shifts", bus.getRegistrationNumber());
                hasViolation = true;
            }

            // Check 3: Different drivers for morning and afternoon
            if (busAssignments.size() == 2) {
                User driver1 = busAssignments.get(0).getDriver();
                User driver2 = busAssignments.get(1).getDriver();
                if (driver1.equals(driver2)) {
                    log.error("VIOLATION: Bus {} has same driver for both shifts: {} {}",
                            bus.getRegistrationNumber(), driver1.getFirstname(), driver1.getLastname());
                    hasViolation = true;
                }
            }
        }

        // Check for duplicate driver assignments (same driver on multiple buses)
        Map<User, List<BusAssignment>> assignmentsByDriver = assignments.stream()
                .collect(Collectors.groupingBy(BusAssignment::getDriver));

        for (Map.Entry<User, List<BusAssignment>> entry : assignmentsByDriver.entrySet()) {
            User driver = entry.getKey();
            List<BusAssignment> driverAssignments = entry.getValue();
            if (driverAssignments.size() > 1) {
                log.error("VIOLATION: Driver {} {} is assigned to {} buses (should be 1)",
                        driver.getFirstname(), driver.getLastname(), driverAssignments.size());
                hasViolation = true;
            }
        }

        if (!hasViolation) {
            log.info("✅ All bus assignment constraints satisfied:");
            log.info("   - Each bus has exactly 2 assignments (morning + afternoon)");
            log.info("   - Each bus has unique shifts");
            log.info("   - Each driver assigned to exactly 1 bus");
        }
    }
}