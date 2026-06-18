package org.tracker.ubus.ubus.TestDataLoader.BusAsssignments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Enum.DriverSchedule;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Order(3)
@Component
@RequiredArgsConstructor
public class BusAssignmentTestDataGenerator implements CommandLineRunner {

    private final BusRepository busRepository;
    private final UserRepository userRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final TransactionTemplate transactionTemplate;
    private final AtomicInteger savedCount = new AtomicInteger(0);

    @Override
    public void run(String... args) throws Exception {
        if (busAssignmentRepository.count() == 0) {
            log.info("🚀 =============================================");
            log.info("🚀 ASSIGNING DRIVERS TO BUSES IN PARALLEL");
            log.info("🚀 =============================================");
            long startTime = System.currentTimeMillis();

            // Get all drivers (users with role DRIVER)
            List<User> allDrivers = userRepository.findByRole(UserRole.DRIVER);
            List<Bus> allBuses = busRepository.findAll();

            if (allDrivers.isEmpty()) {
                log.warn("⚠️ No drivers found! Please run UserTestDataGenerator first.");
                return;
            }

            if (allBuses.isEmpty()) {
                log.warn("⚠️ No buses found! Please run BusTestDataGenerator first.");
                return;
            }

            log.info("📊 Found {} drivers and {} buses", allDrivers.size(), allBuses.size());

            // Shuffle drivers to randomize assignment
            Collections.shuffle(allDrivers);
            Collections.shuffle(allBuses);

            // Track which drivers are already assigned to avoid duplicates
            Set<User> assignedDrivers = Collections.synchronizedSet(new HashSet<>());

            // Track which buses already have morning/evening assignments
            Map<Bus, Set<DriverSchedule>> busSchedules = new HashMap<>();

            int maxAssignments = Math.min(allDrivers.size(), allBuses.size() * 2); // At most 2 drivers per bus
            log.info("🎯 Planning to create {} bus assignments", maxAssignments);

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                int driverIndex = 0;
                int busesAssigned = 0;

                for (Bus bus : allBuses) {
                    // Each bus can have up to 2 drivers (one morning, one evening)
                    Set<DriverSchedule> assignedSchedules = busSchedules.computeIfAbsent(bus, k -> new HashSet<>());

                    // Try to assign morning driver if not already assigned
                    if (!assignedSchedules.contains(DriverSchedule.MORNING_AFTERNOON) && driverIndex < allDrivers.size()) {
                        final User driver = allDrivers.get(driverIndex);
                        final Bus currentBus = bus;

                        if (!assignedDrivers.contains(driver)) {
                            assignedDrivers.add(driver);
                            futures.add(CompletableFuture.runAsync(() ->
                                    assignDriverToBus(driver, currentBus, DriverSchedule.MORNING_AFTERNOON), executor));
                            assignedSchedules.add(DriverSchedule.MORNING_AFTERNOON);
                            driverIndex++;
                            busesAssigned++;
                        }
                    }

                    // Try to assign evening driver if not already assigned
                    if (!assignedSchedules.contains(DriverSchedule.AFTERNOON_EVENING) && driverIndex < allDrivers.size()) {
                        final User driver = allDrivers.get(driverIndex);
                        final Bus currentBus = bus;

                        if (!assignedDrivers.contains(driver)) {
                            assignedDrivers.add(driver);
                            futures.add(CompletableFuture.runAsync(() ->
                                    assignDriverToBus(driver, currentBus, DriverSchedule.AFTERNOON_EVENING), executor));
                            assignedSchedules.add(DriverSchedule.AFTERNOON_EVENING);
                            driverIndex++;
                            busesAssigned++;
                        }
                    }

                    // Progress update
                    if (busesAssigned % 10 == 0 && busesAssigned > 0) {
                        log.info("📊 Progress: Assigned {}/{} drivers to buses", busesAssigned, maxAssignments);
                    }

                    // Stop if we've assigned all drivers
                    if (driverIndex >= allDrivers.size()) {
                        log.info("✅ All {} drivers have been assigned", allDrivers.size());
                        break;
                    }
                }

                // Wait for all assignments to complete
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("🎉 =============================================");
            log.info("✅ COMPLETED! Created {} bus assignments in {} ms", savedCount.get(), elapsed);
            log.info("🎉 =============================================");

            // Summary
            long busesWithMorning = busAssignmentRepository.countByDriverSchedule(DriverSchedule.MORNING_AFTERNOON);
            long busesWithEvening = busAssignmentRepository.countByDriverSchedule(DriverSchedule.AFTERNOON_EVENING);
            log.info("📊 Assignment Summary:");
            log.info("   🌅 Morning schedule: {} buses", busesWithMorning);
            log.info("   🌙 Evening schedule: {} buses", busesWithEvening);

        } else {
            log.info("✅ Bus assignments already exist - skipping data generation");
        }
    }

    private void assignDriverToBus(User driver, Bus bus, DriverSchedule schedule) {
        transactionTemplate.executeWithoutResult(status -> {
            // Check if this bus already has this schedule assigned
            Bus managedBus = busRepository.findByIdWithAssignments(bus.getId());
            boolean alreadyExists = busAssignmentRepository.existsByBusAndDriverSchedule(bus, schedule);
            if (!alreadyExists) {
                BusAssignment assignment = BusAssignment.builder()
                        .driver(driver)
                        .bus(managedBus)
                        .driverSchedule(schedule)
                        .build();
                busAssignmentRepository.save(assignment);
                int saved = savedCount.incrementAndGet();

                if (saved % 5 == 0) {
                    log.debug("✅ Assigned driver {} to bus {} with {} schedule",
                            driver.getFirstname(), bus.getName(), schedule.getLabel());
                }
            } else {
                log.warn("⚠️ Bus {} already has a {} schedule assigned", bus.getName(), schedule.getLabel());
            }
        });
    }
}