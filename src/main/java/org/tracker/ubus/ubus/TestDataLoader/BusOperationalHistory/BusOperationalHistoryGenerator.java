package org.tracker.ubus.ubus.TestDataLoader.BusOperationalHistory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.MaintenanceIssue;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.Priority;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Order(6)
@Component
@RequiredArgsConstructor
public class BusOperationalHistoryGenerator implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private static final int BATCH_SIZE = 5000;
    private final Set<String> spikeDates = new HashSet<>();

    // Bus reliability profiles - specific bus names to assign patterns
    private static final Set<String> UNRELIABLE_BUSES = Set.of(
            "DFC-APK 2",
            "SWC-APB 3",
            "SWC-DFC 4",
            "APK-JBS 1"
    );

    private static final Set<String> RELIABLE_BUSES = Set.of(
            "DFC-APK 1",
            "SWC-APB 1",
            "SWC-DFC 1",
            "APK-JBS 2"
    );

    private static final Set<String> AVERAGE_BUSES = Set.of(
            "DFC-APK 3", "SWC-APB 2", "SWC-APB 4",
            "SWC-APB 5", "SWC-DFC 2", "SWC-DFC 3"
    );

    // Date range: 14 February 2026 to Today
    private static final LocalDate YEAR_START = LocalDate.of(2026, 2, 14);
    private static final LocalDate YEAR_END = LocalDate.now();

    @Override
    public void run(String... args) throws Exception {
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║     BUS OPERATIONAL HISTORY GENERATOR                     ║");
        log.info("║     Date Range: {} to {}                                  ║", YEAR_START, YEAR_END);
        log.info("╚════════════════════════════════════════════════════════════╝");
        // Uncomment to run:
        //fastDelete();
        //generateOperationalHistory();
    }

    private void fastDelete() {
        log.info("🗑️  Deleting existing operational history...");
        jdbcTemplate.execute("TRUNCATE TABLE bus_operational_history CASCADE");
        log.info("✅ Deleted");
    }

    private void generateOperationalHistory() {
        long startTime = System.currentTimeMillis();

        Long existingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bus_operational_history", Long.class);

        if (existingCount != null && existingCount > 0) {
            log.info("✅ Already {} operational history records exist. Skipping.", existingCount);
            return;
        }

        // Get all active buses
        List<BusInfo> allBuses = jdbcTemplate.query(
                """
                SELECT id, name, type 
                FROM bus 
                WHERE is_active = true
                """,
                (rs, rowNum) -> new BusInfo(
                        rs.getObject("id", UUID.class),
                        rs.getString("name"),
                        rs.getString("type")
                )
        );

        if (allBuses.isEmpty()) {
            log.error("❌ No buses found!");
            return;
        }

        log.info("📊 Found {} active buses", allBuses.size());

        // Assign consistent reliability profiles per bus
        Map<UUID, BusProfile> busProfiles = new HashMap<>();

        for (BusInfo bus : allBuses) {
            String name = bus.name;
            BusProfile profile;

            if (UNRELIABLE_BUSES.contains(name)) {
                profile = new BusProfile(
                        0.35 + ThreadLocalRandom.current().nextDouble(0.25),
                        "UNRELIABLE",
                        40 + ThreadLocalRandom.current().nextInt(25),
                        true
                );
            } else if (RELIABLE_BUSES.contains(name)) {
                profile = new BusProfile(
                        0.82 + ThreadLocalRandom.current().nextDouble(0.13),
                        "RELIABLE",
                        5 + ThreadLocalRandom.current().nextInt(11),
                        false
                );
            } else {
                profile = new BusProfile(
                        0.60 + ThreadLocalRandom.current().nextDouble(0.20),
                        "AVERAGE",
                        20 + ThreadLocalRandom.current().nextInt(16),
                        ThreadLocalRandom.current().nextBoolean()
                );
            }

            busProfiles.put(bus.id, profile);
        }

        log.info("📊 Reliability Distribution:");
        log.info("   Unreliable buses (40-60% issue rate): {}", UNRELIABLE_BUSES);
        log.info("   Reliable buses (5-15% issue rate): {}", RELIABLE_BUSES);
        log.info("   Average buses (20-35% issue rate): {}", AVERAGE_BUSES);

        // Generate all dates from YEAR_START to YEAR_END
        List<LocalDate> allDates = new ArrayList<>();
        LocalDate date = YEAR_START;
        while (!date.isAfter(YEAR_END)) {
            allDates.add(date);
            date = date.plusDays(1);
        }

        log.info("📊 Generating records for {} days ({} to {})",
                allDates.size(), allDates.get(0), allDates.get(allDates.size() - 1));

        calculateSpikePeriods(allDates);
        log.info("   Generated {} spike days", spikeDates.size());

        // Track issue frequency per bus
        Map<UUID, Integer> busIssueFrequency = new HashMap<>();
        Map<UUID, Integer> busConsecutiveIssues = new HashMap<>();
        LocalDate today = LocalDate.now();

        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        int count = 0;
        int operationalCount = 0;
        int maintenanceCount = 0;
        int outOfServiceCount = 0;
        int issueCount = 0;
        int criticalCount = 0;
        int majorCount = 0;
        int minorCount = 0;

        for (LocalDate currentDate : allDates) {
            String dateStr = currentDate.toString();
            boolean isWeekend = currentDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
                    currentDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
            boolean isSpikePeriod = spikeDates.contains(dateStr);
            boolean isHoliday = isHoliday(currentDate);
            boolean isSeasonal = isSeasonalIssue(currentDate);
            boolean isPostHoliday = isPostHolidayPeriod(currentDate);

            for (BusInfo bus : allBuses) {
                UUID busId = bus.id;
                BusProfile profile = busProfiles.get(busId);
                BusOperationalStatus status;
                MaintenanceIssue issue = null;
                Priority priority = null;
                String description = null;
                LocalDate dateResolved = null;

                int previousIssues = busIssueFrequency.getOrDefault(busId, 0);
                int consecutiveIssues = busConsecutiveIssues.getOrDefault(busId, 0);

                boolean isOffDay = isWeekend || isHoliday;

                if (isOffDay) {
                    int offDayMaintenanceChance = profile.baseIssueRate / 2;

                    if (ThreadLocalRandom.current().nextInt(100) < offDayMaintenanceChance) {
                        status = BusOperationalStatus.MAINTENANCE;
                        issue = getRealisticIssue(currentDate, bus.type);
                        priority = getRealisticPriority(issue, status, isSpikePeriod);
                        description = getRealisticDescription(issue, bus.type, status);

                        int daysToResolve = ThreadLocalRandom.current().nextInt(1, 3);
                        dateResolved = currentDate.plusDays(daysToResolve);

                        busIssueFrequency.merge(busId, 1, Integer::sum);
                        busConsecutiveIssues.merge(busId, 1, Integer::sum);
                        issueCount++;
                        maintenanceCount++;

                        if (priority == Priority.CRITICAL) criticalCount++;
                        else if (priority == Priority.MAJOR) majorCount++;
                        else minorCount++;
                    } else {
                        status = BusOperationalStatus.OPERATIONAL;
                        operationalCount++;
                        busConsecutiveIssues.put(busId, 0);
                    }
                } else {
                    int issueRate = profile.baseIssueRate;

                    if (isSpikePeriod) issueRate += 15;
                    if (isSeasonal) {
                        if ("ELECTRIC".equals(bus.type)) issueRate += 10;
                        else issueRate += 5;
                    }
                    if (isPostHoliday) issueRate += 5;
                    if (consecutiveIssues > 3) issueRate += 10;
                    else if (consecutiveIssues > 1) issueRate += 5;

                    issueRate = Math.min(issueRate, 85);

                    int rand = ThreadLocalRandom.current().nextInt(100);

                    if (rand < issueRate) {
                        if (profile.proneToSevereIssues && ThreadLocalRandom.current().nextDouble() < 0.3) {
                            status = BusOperationalStatus.OUT_OF_SERVICE;
                            int daysToResolve = ThreadLocalRandom.current().nextInt(3, 15);
                            dateResolved = currentDate.plusDays(daysToResolve);
                        } else {
                            status = BusOperationalStatus.MAINTENANCE;
                            int daysToResolve = ThreadLocalRandom.current().nextInt(1, 4);
                            dateResolved = currentDate.plusDays(daysToResolve);
                        }

                        issue = getRealisticIssue(currentDate, bus.type);
                        priority = getRealisticPriority(issue, status, isSpikePeriod);
                        description = getRealisticDescription(issue, bus.type, status);

                        busIssueFrequency.merge(busId, 1, Integer::sum);
                        busConsecutiveIssues.merge(busId, 1, Integer::sum);
                        issueCount++;

                        if (priority == Priority.CRITICAL) criticalCount++;
                        else if (priority == Priority.MAJOR) majorCount++;
                        else minorCount++;

                        if (status == BusOperationalStatus.OUT_OF_SERVICE) {
                            outOfServiceCount++;
                        } else {
                            maintenanceCount++;
                        }
                    } else {
                        status = BusOperationalStatus.OPERATIONAL;
                        operationalCount++;
                        busConsecutiveIssues.put(busId, 0);
                    }
                }

                batch.add(new Object[]{
                        UUID.randomUUID(),
                        busId,
                        status.name(),
                        issue != null ? issue.name() : null,
                        priority != null ? priority.name() : null,
                        description,
                        dateResolved,
                        currentDate
                });
                count++;

                if (batch.size() >= BATCH_SIZE) {
                    jdbcTemplate.batchUpdate(
                            """
                            INSERT INTO bus_operational_history 
                            (id, bus_id, bus_operational_status, maintenance_issue, priority, description, date_resolved, date_operated) 
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                            batch
                    );
                    batch.clear();

                    if (count % 50000 == 0) {
                        log.info("   📝 Inserted {} records...", count);
                    }
                }
            }
        }

        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    """
                    INSERT INTO bus_operational_history 
                    (id, bus_id, bus_operational_status, maintenance_issue, priority, description, date_resolved, date_operated) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    batch
            );
        }

        long endTime = System.currentTimeMillis();
        log.info("");
        log.info("📊 Summary Statistics:");
        log.info("   Date Range: {} to {}", YEAR_START, YEAR_END);
        log.info("   Total days: {}", allDates.size());
        log.info("   Total buses: {}", allBuses.size());
        log.info("   Total records inserted: {}", count);
        log.info("   Operational: {} ({}%)", operationalCount,
                String.format("%.1f", (double) operationalCount / count * 100));
        log.info("   Maintenance: {} ({}%)", maintenanceCount,
                String.format("%.1f", (double) maintenanceCount / count * 100));
        log.info("   Out of Service: {} ({}%)", outOfServiceCount,
                String.format("%.1f", (double) outOfServiceCount / count * 100));
        log.info("   Issues recorded: {}", issueCount);
        log.info("   Priority Distribution:");
        log.info("      CRITICAL: {} ({}%)", criticalCount,
                String.format("%.1f", issueCount > 0 ? (double) criticalCount / issueCount * 100 : 0));
        log.info("      MAJOR: {} ({}%)", majorCount,
                String.format("%.1f", issueCount > 0 ? (double) majorCount / issueCount * 100 : 0));
        log.info("      MINOR: {} ({}%)", minorCount,
                String.format("%.1f", issueCount > 0 ? (double) minorCount / issueCount * 100 : 0));
        log.info("   Records per bus: {}", count / allBuses.size());
        log.info("✅ Inserted in {} seconds", (endTime - startTime) / 1000);


    }

    private boolean isHoliday(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        return (month == 3 && day == 21) ||
                (month == 4 && day == 6) ||
                (month == 4 && day == 27) ||
                (month == 5 && day == 1) ||
                (month == 5 && day == 25) ||
                (month == 6 && day == 16) ||
                (month == 9 && day == 24) ||
                (month == 12 && day == 16) ||
                (month == 12 && day == 25) ||
                (month == 12 && day == 26);
    }

    private boolean isSeasonalIssue(LocalDate date) {
        int month = date.getMonthValue();
        return month == 6 || month == 7 || month == 8;
    }

    private boolean isPostHolidayPeriod(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        if (month == 1 && day >= 6 && day <= 20) return true;
        if (month == 4 && day >= 15 && day <= 25) return true;
        if (month == 9 && day >= 1 && day <= 15) return true;
        return false;
    }

    private void calculateSpikePeriods(List<LocalDate> allDates) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        spikeDates.clear();

        for (LocalDate date : allDates) {
            int month = date.getMonthValue();
            int day = date.getDayOfMonth();

            if (month == 6 || month == 7) {
                if (random.nextDouble() < 0.6) {
                    spikeDates.add(date.toString());
                }
                continue;
            }

            if (month == 8) {
                if (random.nextDouble() < 0.25) {
                    spikeDates.add(date.toString());
                }
                continue;
            }

            if (month == 1 && day > 10 && day < 25) {
                if (random.nextDouble() < 0.5) {
                    spikeDates.add(date.toString());
                }
                continue;
            }
            if (month == 2 && day < 15) {
                if (random.nextDouble() < 0.4) {
                    spikeDates.add(date.toString());
                }
                continue;
            }

            if (month == 1 && day >= 6 && day <= 20) {
                if (random.nextDouble() < 0.5) {
                    spikeDates.add(date.toString());
                }
                continue;
            }

            if (month == 4 && day >= 15 && day <= 25) {
                if (random.nextDouble() < 0.5) {
                    spikeDates.add(date.toString());
                }
                continue;
            }

            if (month == 9 && day >= 1 && day <= 15) {
                if (random.nextDouble() < 0.5) {
                    spikeDates.add(date.toString());
                }
                continue;
            }

            if (random.nextDouble() < 0.05) {
                spikeDates.add(date.toString());
            }
        }

        log.info("   Spike days: {} ({}% of total)",
                spikeDates.size(),
                String.format("%.1f", (double) spikeDates.size() / allDates.size() * 100));
    }

    private Priority getRealisticPriority(MaintenanceIssue issue, BusOperationalStatus status, boolean isSpikePeriod) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        if (status == BusOperationalStatus.OUT_OF_SERVICE) {
            // Out of service = CRITICAL or MAJOR, never MINOR
            return random.nextDouble() < 0.6 ? Priority.CRITICAL : Priority.MAJOR;
        }

        // For MAINTENANCE status - MINOR issues should be very common
        // Target: 70% MINOR, 25% MAJOR, 5% CRITICAL

        // ENGINE issues - more serious
        if (issue == MaintenanceIssue.ENGINE) {
            if (random.nextDouble() < 0.10) return Priority.CRITICAL;
            else if (random.nextDouble() < 0.40) return Priority.MAJOR;
            else return Priority.MINOR;
        }

        // ELECTRICAL issues - usually minor
        if (issue == MaintenanceIssue.ELECTRICAL) {
            if (random.nextDouble() < 0.05) return Priority.CRITICAL;
            else if (random.nextDouble() < 0.25) return Priority.MAJOR;
            else return Priority.MINOR;
        }

        // BODY issues - mostly minor
        if (issue == MaintenanceIssue.BODY) {
            if (random.nextDouble() < 0.03) return Priority.CRITICAL;
            else if (random.nextDouble() < 0.15) return Priority.MAJOR;
            else return Priority.MINOR;
        }

        // TIRES - mostly minor
        if (issue == MaintenanceIssue.TIRES) {
            if (random.nextDouble() < 0.02) return Priority.CRITICAL;
            else if (random.nextDouble() < 0.15) return Priority.MAJOR;
            else return Priority.MINOR;
        }

        // OTHER - mostly minor
        if (issue == MaintenanceIssue.OTHER) {
            if (random.nextDouble() < 0.03) return Priority.CRITICAL;
            else if (random.nextDouble() < 0.20) return Priority.MAJOR;
            else return Priority.MINOR;
        }

        // Spike period = slightly more serious issues
        if (isSpikePeriod) {
            if (random.nextDouble() < 0.10) return Priority.CRITICAL;
            else if (random.nextDouble() < 0.35) return Priority.MAJOR;
            else return Priority.MINOR;
        }

        // Default: 5% CRITICAL, 25% MAJOR, 70% MINOR
        int rand = random.nextInt(100);
        if (rand < 5) return Priority.CRITICAL;
        else if (rand < 30) return Priority.MAJOR;
        else return Priority.MINOR;
    }

    private MaintenanceIssue getRealisticIssue(LocalDate date, String busType) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int rand = random.nextInt(100);

        boolean isRainy = date.getMonthValue() == 6 || date.getMonthValue() == 7 || date.getMonthValue() == 8;
        boolean isHot = date.getMonthValue() == 1 || date.getMonthValue() == 2;

        if ("ELECTRIC".equals(busType)) {
            if (isRainy) {
                if (rand < 55) return MaintenanceIssue.ELECTRICAL;
                else if (rand < 80) return MaintenanceIssue.TIRES;
                else if (rand < 95) return MaintenanceIssue.BODY;
                else return MaintenanceIssue.OTHER;
            } else if (isHot) {
                if (rand < 50) return MaintenanceIssue.ELECTRICAL;
                else if (rand < 75) return MaintenanceIssue.TIRES;
                else if (rand < 90) return MaintenanceIssue.BODY;
                else return MaintenanceIssue.OTHER;
            } else {
                if (rand < 45) return MaintenanceIssue.ELECTRICAL;
                else if (rand < 70) return MaintenanceIssue.TIRES;
                else if (rand < 85) return MaintenanceIssue.BODY;
                else return MaintenanceIssue.OTHER;
            }
        }

        // COMBUSTION buses
        if (isRainy) {
            if (rand < 35) return MaintenanceIssue.ELECTRICAL;
            else if (rand < 60) return MaintenanceIssue.ENGINE;
            else if (rand < 80) return MaintenanceIssue.BODY;
            else if (rand < 95) return MaintenanceIssue.TIRES;
            else return MaintenanceIssue.OTHER;
        } else if (isHot) {
            if (rand < 40) return MaintenanceIssue.ENGINE;
            else if (rand < 65) return MaintenanceIssue.TIRES;
            else if (rand < 80) return MaintenanceIssue.ELECTRICAL;
            else if (rand < 95) return MaintenanceIssue.BODY;
            else return MaintenanceIssue.OTHER;
        } else {
            if (rand < 35) return MaintenanceIssue.ENGINE;
            else if (rand < 60) return MaintenanceIssue.ELECTRICAL;
            else if (rand < 80) return MaintenanceIssue.TIRES;
            else if (rand < 95) return MaintenanceIssue.BODY;
            else return MaintenanceIssue.OTHER;
        }
    }

    private String getRealisticDescription(MaintenanceIssue issue, String busType, BusOperationalStatus status) {
        List<String> descriptions = new ArrayList<>();

        switch (issue) {
            case ENGINE -> {
                if ("ELECTRIC".equals(busType)) {
                    descriptions.addAll(Arrays.asList(
                            "Motor running hotter than a summer sidewalk - needs cooling system check",
                            "Battery cooling system malfunction - temperature regulation failed",
                            "Inverter communication error - power loss detected",
                            "Regenerative braking system fault - efficiency reduced",
                            "Motor controller requires recalibration - erratic power delivery",
                            "High voltage battery pack showing imbalance - cell balancing required",
                            "Electric motor making grinding noises - bearing failure imminent",
                            "Coolant pump failure - battery overheating",
                            "Motor insulation resistance low - short circuit risk",
                            "Drive unit vibrating excessively - mounting bolts loose"
                    ));
                } else {
                    descriptions.addAll(Arrays.asList(
                            "Engine overheating - cooling system failure",
                            "Engine knocking - fuel injection issue",
                            "Engine starting failure - starter motor or fuel system",
                            "Oil pressure warning - oil leak detected",
                            "Engine misfiring - ignition system fault",
                            "Check engine light active - diagnostic code P0300",
                            "Timing belt showing wear - replacement recommended",
                            "Fuel system contamination - injectors need cleaning",
                            "Engine burning oil - piston rings worn",
                            "Turbocharger whistling - bearings failing"
                    ));
                }
            }
            case ELECTRICAL -> {
                if ("ELECTRIC".equals(busType)) {
                    descriptions.addAll(Arrays.asList(
                            "Battery pack voltage imbalance - cell balancing required",
                            "Charging system fault - reduced charging rate",
                            "High voltage battery warning - range degradation detected",
                            "Battery management system error - communication failure",
                            "Range dropped significantly - battery health concern",
                            "Thermal management system fault - battery temperature high",
                            "DC-DC converter failure - aux battery not charging",
                            "Battery discharging rapidly - parasitic drain",
                            "High voltage contactor stuck - bus won't power on",
                            "Cell voltage critically low - cell failure"
                    ));
                } else {
                    descriptions.addAll(Arrays.asList(
                            "Battery discharging rapidly - alternator issue",
                            "Alternator failure - running on battery only",
                            "Headlights flickering - voltage regulator problem",
                            "Dashboard warning lights - multiple electrical faults",
                            "Starter motor failure - no crank condition",
                            "Electrical short detected - wiring harness inspection needed",
                            "Fuse box corrosion - multiple circuits affected",
                            "Battery terminals corroded - poor connection",
                            "Wiring harness chafing - bare wires touching metal",
                            "Instrument cluster failure - no gauges"
                    ));
                }
            }
            case BODY -> descriptions.addAll(Arrays.asList(
                    "Passenger door stuck - handle mechanism failure",
                    "Windshield cracked - needs replacement",
                    "Side mirror damaged - visibility compromised",
                    "Seat cushion torn - passenger comfort issue",
                    "Floorpan corrosion - structural integrity concern",
                    "Roof panel dented - body repair needed",
                    "Door alignment issue - closing properly affected",
                    "Window regulator failure - window stuck open",
                    "Emergency exit handle broken - safety concern",
                    "Rust spreading - body panels need attention"
            ));
            case TIRES -> descriptions.addAll(Arrays.asList(
                    "Flat tire - puncture repair or replacement needed",
                    "Tread depth below legal limit - replacement required",
                    "Tire blowout - sidewall damage",
                    "Uneven tire wear - alignment needed",
                    "Valve stem leaking - slow leak detected",
                    "TPMS warning - tire pressure sensor failure",
                    "Tire age deterioration - dry rot visible",
                    "Tire has bulge - sidewall about to blow",
                    "Wheel bent - causing vibration",
                    "Lug nuts loose - wheel about to fall off"
            ));
            default -> descriptions.addAll(Arrays.asList(
                    "Suspension worn - rough ride quality",
                    "Brake pads worn - reduced stopping power",
                    "Transmission slipping - gear engagement issues",
                    "Steering wheel vibration - alignment or balance issue",
                    "Exhaust system leak - emissions concern",
                    "Air conditioning failure - no cooling",
                    "Fuel gauge inaccurate - sending unit failure",
                    "GPS tracking system offline - location data missing",
                    "Wiper motor failure - reduced visibility in rain",
                    "Mirror adjustment motor failed - limited visibility"
            ));
        }

        String base = descriptions.get(ThreadLocalRandom.current().nextInt(descriptions.size()));

        if (status == BusOperationalStatus.OUT_OF_SERVICE) {
            return base + " - CRITICAL: Bus out of service until repaired";
        } else if (status == BusOperationalStatus.MAINTENANCE) {
            return base + " - scheduled maintenance required";
        }
        return base;
    }


    private record BusProfile(double reliability, String label, int baseIssueRate, boolean proneToSevereIssues) {}

    private record BusInfo(UUID id, String name, String type) {}
}