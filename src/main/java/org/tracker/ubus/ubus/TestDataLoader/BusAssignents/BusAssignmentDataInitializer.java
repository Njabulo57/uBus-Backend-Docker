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
        List<Bus> buses = busRepository.findAll();
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

        // Create bus assignments
        List<BusAssignment> assignments = createBusAssignments(buses, drivers);

        // Save all assignments
        busAssignmentRepository.saveAll(assignments);

        log.info("Successfully created {} bus assignments", assignments.size());

        // Log summary
        logBusAssignmentSummary(assignments);
    }

    private List<BusAssignment> createBusAssignments(List<Bus> buses, List<User> drivers) {
        List<BusAssignment> assignments = new ArrayList<>();
        Queue<User> availableDrivers = new LinkedList<>(drivers);

        for (Bus bus : buses) {
            // For each bus, create up to 2 assignments (morning and evening)

            // Morning assignment (Morning to Afternoon shift)
            if (!availableDrivers.isEmpty()) {
                User morningDriver = availableDrivers.poll();
                BusAssignment morningAssignment = BusAssignment.builder()
                        .bus(bus)
                        .driver(morningDriver)
                        .driverSchedule(DriverSchedule.MORNING_AFTERNOON)
                        .build();
                assignments.add(morningAssignment);
            }

            // Evening assignment (Afternoon to Evening shift)
            // Only create if we have enough drivers
            if (!availableDrivers.isEmpty()) {
                User eveningDriver = availableDrivers.poll();
                BusAssignment eveningAssignment = BusAssignment.builder()
                        .bus(bus)
                        .driver(eveningDriver)
                        .driverSchedule(DriverSchedule.AFTERNOON_EVENING)
                        .build();
                assignments.add(eveningAssignment);
            }

            // If we run out of drivers, break
            if (availableDrivers.isEmpty()) {
                log.warn("Ran out of drivers after assigning {} buses", assignments.size() / 2);
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
            log.info("Bus: {} (Capacity: {})",
                    bus.getRegistrationNumber(),
                    bus.getCapacity());

            busAssignments.forEach(assignment -> {
                log.info("  - {} shift: Driver {} {} ({} - {})",
                        assignment.getDriverSchedule().getLabel(),
                        assignment.getDriver().getFirstname(),
                        assignment.getDriver().getLastname(),
                        assignment.getDriverSchedule().getStartTime(),
                        assignment.getDriverSchedule().getEndTime());
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
        }

        if (!hasViolation) {
            log.info("✅ All bus assignment constraints satisfied:");
            log.info("   - Each bus has maximum 2 assignments");
            log.info("   - Each bus has unique shifts (morning/evening)");
        }
    }
}