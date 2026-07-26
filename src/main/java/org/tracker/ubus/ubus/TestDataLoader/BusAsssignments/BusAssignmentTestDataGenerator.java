package org.tracker.ubus.ubus.TestDataLoader.BusAsssignments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
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
@Order(3)
@Component
@RequiredArgsConstructor
public class BusAssignmentTestDataGenerator implements CommandLineRunner {

    private final BusRepository busRepository;
    private final UserRepository userRepository;
    private final BusAssignmentRepository busAssignmentRepository;

    // All buses available in the system - 7 of each type (Total: 21 buses)
    private static final List<String> ALL_BUS_NAMES = Arrays.asList(
            // DFC Buses (Route 1 & 3)
            "DFC 1", "DFC 2", "DFC 3", "DFC 4", "DFC 5", "DFC 6", "DFC 7",
            // SWC Buses (Route 2 & 3)
            "SWC 1", "SWC 2", "SWC 3", "SWC 4", "SWC 5", "SWC 6", "SWC 7",
            // JBS Buses (JBS Route)
            "JBS 1", "JBS 2", "JBS 3", "JBS 4", "JBS 5", "JBS 6", "JBS 7"
    );

    // Select 4 buses from each type (4 DFC + 4 SWC + 4 JBS = 12 buses total)
    private static final List<String> SELECTED_BUS_NAMES = Arrays.asList(
            // 4 DFC Buses
            "DFC 1", "DFC 3", "DFC 5", "DFC 7",
            // 4 SWC Buses
            "SWC 1", "SWC 3", "SWC 5", "SWC 7",
            // 4 JBS Buses
            "JBS 1", "JBS 3", "JBS 5", "JBS 7"
    );

    @Override
    public void run(String... args) {
        if (busAssignmentRepository.count() > 0) {
            log.info("✅ Bus assignments already exist - skipping data generation");
            return;
        }

        log.info("🚀 =============================================");
        log.info("🚀 ASSIGNING ACTIVE DRIVERS TO SELECT BUSES");
        log.info("🚀 =============================================");
        long startTime = System.currentTimeMillis();

        // ✅ ONLY fetch ACTIVE drivers
        List<User> activeDrivers = userRepository.findByRoleAndStatus(UserRole.DRIVER, UserStatus.ACTIVE);
        List<Bus> allBuses = busRepository.findAllBuses();

        if (activeDrivers.isEmpty()) {
            log.warn("⚠️ No ACTIVE drivers found! Please run UserTestDataGenerator first.");
            return;
        }

        if (allBuses.isEmpty()) {
            log.warn("⚠️ No buses found! Please run BusTestDataGenerator first.");
            return;
        }

        log.info("📊 Found {} ACTIVE drivers and {} buses", activeDrivers.size(), allBuses.size());

        // Show all buses available in the system
        log.info("📋 All buses in system ({} total):", allBuses.size());
        Map<String, List<Bus>> busesByType = allBuses.stream()
                .collect(Collectors.groupingBy(bus -> {
                    String name = bus.getName();
                    if (name.startsWith("DFC")) return "DFC";
                    if (name.startsWith("SWC")) return "SWC";
                    if (name.startsWith("JBS")) return "JBS";
                    return "OTHER";
                }));

        for (Map.Entry<String, List<Bus>> entry : busesByType.entrySet()) {
            log.info("   {}: {} buses", entry.getKey(), entry.getValue().size());
        }

        // Filter to selected buses (4 from each type)
        List<Bus> targetBuses = allBuses.stream()
                .filter(bus -> SELECTED_BUS_NAMES.contains(bus.getName()))
                .filter(Bus::isActive)  // Only active buses
                .toList();

        log.info("📊 Selected target buses: {} buses (4 from each type)", targetBuses.size());

        // Group selected buses by type for better logging
        Map<String, List<Bus>> selectedByType = targetBuses.stream()
                .collect(Collectors.groupingBy(bus -> {
                    String name = bus.getName();
                    if (name.startsWith("DFC")) return "DFC";
                    if (name.startsWith("SWC")) return "SWC";
                    if (name.startsWith("JBS")) return "JBS";
                    return "OTHER";
                }));

        for (Map.Entry<String, List<Bus>> entry : selectedByType.entrySet()) {
            log.info("   {}: {} buses - {}", entry.getKey(), entry.getValue().size(),
                    entry.getValue().stream().map(Bus::getName).collect(Collectors.joining(", ")));
        }

        // Check if any selected buses are missing
        List<String> missingBuses = SELECTED_BUS_NAMES.stream()
                .filter(name -> targetBuses.stream().noneMatch(bus -> bus.getName().equals(name)))
                .toList();

        if (!missingBuses.isEmpty()) {
            log.warn("⚠️ Some selected buses not found in the system: {}", missingBuses);
            log.warn("   💡 Please create these buses in BusTestDataGenerator");
        }

        // Shuffle drivers to randomize assignment
        Collections.shuffle(activeDrivers);

        // Track which drivers have been assigned
        Set<User> assignedDrivers = new HashSet<>();
        List<BusAssignment> batch = new ArrayList<>();

        int driverIndex = 0;
        int assignmentsCreated = 0;

        // Each bus gets exactly 2 drivers (morning + evening)
        int driversPerBus = 2;
        int totalDriversNeeded = targetBuses.size() * driversPerBus;

        log.info("📝 Assignment plan: {} drivers needed ({} buses × 2 drivers each)", totalDriversNeeded, targetBuses.size());
        log.info("   🎯 Pattern: Morning + Evening driver for each bus");
        log.info("   📋 Route coverage:");
        log.info("      🚌 DFC Buses (4) → Route 1: DFC ↔ APB ↔ APK & Route 3: SWC ↔ DFC");
        log.info("      🚌 SWC Buses (4) → Route 2: SWC ↔ Kingsway ↔ Bunting & Route 3: SWC ↔ DFC");
        log.info("      🚌 JBS Buses (4) → JBS Route: APK ↔ APB ↔ JBS");

        // Check if we have enough drivers
        if (activeDrivers.size() < totalDriversNeeded) {
            log.warn("⚠️ Not enough active drivers! Need {} but only have {}", totalDriversNeeded, activeDrivers.size());
            log.warn("   💡 Will assign as many as possible");
        }

        for (Bus bus : targetBuses) {
            int driversAssignedToThisBus = 0;

            // Assign Morning driver (MORNING_AFTERNOON shift)
            if (driverIndex < activeDrivers.size()) {
                User morningDriver = activeDrivers.get(driverIndex);
                if (!assignedDrivers.contains(morningDriver)) {
                    batch.add(BusAssignment.builder()
                            .driver(morningDriver)
                            .bus(bus)
                            .driverSchedule(DriverSchedule.MORNING_AFTERNOON)
                            .build());
                    assignedDrivers.add(morningDriver);
                    driverIndex++;
                    driversAssignedToThisBus++;
                    assignmentsCreated++;
                }
            }

            // Assign Evening driver (AFTERNOON_EVENING shift)
            if (driverIndex < activeDrivers.size()) {
                User eveningDriver = activeDrivers.get(driverIndex);
                if (!assignedDrivers.contains(eveningDriver)) {
                    batch.add(BusAssignment.builder()
                            .driver(eveningDriver)
                            .bus(bus)
                            .driverSchedule(DriverSchedule.AFTERNOON_EVENING)
                            .build());
                    assignedDrivers.add(eveningDriver);
                    driverIndex++;
                    driversAssignedToThisBus++;
                    assignmentsCreated++;
                }
            }

            // Log the assignment result for this bus
            String busType = bus.getName().startsWith("DFC") ? "DFC" :
                    bus.getName().startsWith("SWC") ? "SWC" : "JBS";
            log.info("   🚌 {}-{}: {} driver(s) assigned (morning + evening)",
                    busType, bus.getName(), driversAssignedToThisBus);

            if (driversAssignedToThisBus < 2) {
                log.warn("      ⚠️ Could only assign {} out of 2 drivers - not enough active drivers available",
                        driversAssignedToThisBus);
            }
        }

        // Save all assignments in one batch
        if (!batch.isEmpty()) {
            busAssignmentRepository.saveAll(batch);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("🎉 =============================================");
        log.info("✅ COMPLETED! Created {} bus assignments in {} ms", batch.size(), elapsed);
        log.info("🎉 =============================================");

        // Summary statistics
        long morningCount = batch.stream()
                .filter(a -> a.getDriverSchedule() == DriverSchedule.MORNING_AFTERNOON)
                .count();
        long eveningCount = batch.size() - morningCount;

        log.info("📊 Assignment Summary:");
        log.info("   🚌 Buses with assignments: {} out of {} selected",
                batch.stream().map(BusAssignment::getBus).distinct().count(), targetBuses.size());
        log.info("   👤 Drivers assigned: {} out of {} active drivers", assignedDrivers.size(), activeDrivers.size());
        log.info("   🌅 Morning schedule (MORNING_AFTERNOON): {} assignments", morningCount);
        log.info("   🌙 Evening schedule (AFTERNOON_EVENING): {} assignments", eveningCount);

        // Show distribution by bus type
        log.info("   📊 Distribution by bus type:");
        Map<String, Long> typeCount = batch.stream()
                .collect(Collectors.groupingBy(
                        a -> {
                            String name = a.getBus().getName();
                            if (name.startsWith("DFC")) return "DFC";
                            if (name.startsWith("SWC")) return "SWC";
                            if (name.startsWith("JBS")) return "JBS";
                            return "OTHER";
                        },
                        Collectors.counting()
                ));

        for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
            log.info("      {}: {} assignments ({} buses × 2 drivers)",
                    entry.getKey(), entry.getValue(), entry.getValue() / 2);
        }

        // Show actual distribution per bus
        log.info("   📊 Actual distribution per bus:");
        Map<Bus, Long> busDriverCount = batch.stream()
                .collect(Collectors.groupingBy(BusAssignment::getBus, Collectors.counting()));
        for (Bus bus : targetBuses) {
            long count = busDriverCount.getOrDefault(bus, 0L);
            String type = bus.getName().startsWith("DFC") ? "DFC" :
                    bus.getName().startsWith("SWC") ? "SWC" : "JBS";
            log.info("      {}-{}: {} driver(s) (target: 2)", type, bus.getName(), count);
        }

        if (batch.size() < totalDriversNeeded) {
            int shortfall = totalDriversNeeded - batch.size();
            log.warn("⚠️ {} fewer assignments than planned. Not enough active drivers.", shortfall);
            log.warn("   💡 Need {} active drivers for full assignment, have {}",
                    totalDriversNeeded, activeDrivers.size());
        }

        // Show unassigned drivers count
        int unassignedDrivers = activeDrivers.size() - assignedDrivers.size();
        if (unassignedDrivers > 0) {
            log.info("   👤 Unassigned active drivers: {} (left as spare)", unassignedDrivers);
        }
    }
}