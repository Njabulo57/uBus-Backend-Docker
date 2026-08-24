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
    // Pre-calculated spike periods for realistic patterns
    private final Set<String> spikeDates = new HashSet<>();

    @Override
    public void run(String... args) throws Exception {
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║     BUS OPERATIONAL HISTORY GENERATOR - REALISTIC         ║");
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

        // Get all active buses with their type
        List<BusInfo> allBuses = jdbcTemplate.query(
                """
                SELECT id, type 
                FROM bus 
                WHERE is_active = true
                """,
                (rs, rowNum) -> new BusInfo(
                        rs.getObject("id", UUID.class),
                        rs.getString("type")
                )
        );

        if (allBuses.isEmpty()) {
            log.error("❌ No buses found!");
            return;
        }

        log.info("📊 Found {} active buses", allBuses.size());

        long electricCount = allBuses.stream().filter(b -> "ELECTRIC".equals(b.type)).count();
        long combustionCount = allBuses.stream().filter(b -> "COMBUSTION".equals(b.type)).count();
        log.info("   Electric: {}, Combustion: {}", electricCount, combustionCount);

        // Get all trip dates.
        // NOTE: filter out NULL departure_time at the source — some trips
        // (e.g. from the live/simulated-trips feature) can be inserted
        // without a departure_time set. A NULL departure_time::date sorts
        // LAST in Postgres' default ascending order, so without this filter
        // it silently lands at the end of the list and blows up the very
        // first date-based calculation downstream with an NPE.
        List<LocalDate> tripDates = jdbcTemplate.queryForList(
                """
                SELECT DISTINCT departure_time::date
                FROM trip
                WHERE departure_time IS NOT NULL
                ORDER BY departure_time::date
                """,
                LocalDate.class);

        // Defensive backstop in case a null ever slips through the query
        // (e.g. a driver-level date function returning null on odd input).
        int beforeFilter = tripDates.size();
        tripDates.removeIf(Objects::isNull);
        if (tripDates.size() != beforeFilter) {
            log.warn("⚠️ Filtered out {} null trip date(s) that slipped past the SQL filter",
                    beforeFilter - tripDates.size());
        }

        if (tripDates.isEmpty()) {
            log.error("❌ No trips with a valid departure_time found!");
            return;
        }

        log.info("📊 Found {} dates with trips ({} to {})",
                tripDates.size(), tripDates.get(0), tripDates.get(tripDates.size() - 1));

        // PRE-CALCULATE SPIKE PERIODS (contiguous blocks, not random days)
        calculateSpikePeriods(tripDates);
        log.info("   Generated {} spike days", spikeDates.size());

        // Get buses used on each date
        Map<String, Set<UUID>> busesUsedByDate = new HashMap<>();
        for (LocalDate date : tripDates) {
            String dateStr = date.toString();
            List<UUID> usedBusIds = jdbcTemplate.queryForList(
                    """
                    SELECT DISTINCT ba.bus_id
                    FROM trip t
                    JOIN bus_assignment ba ON t.bus_assignment_id = ba.id
                    WHERE t.departure_time::date = ?
                    """,
                    UUID.class,
                    date
            );
            busesUsedByDate.put(dateStr, new HashSet<>(usedBusIds));
        }

        // Track issue frequency per bus
        Map<UUID, Integer> busIssueFrequency = new HashMap<>();
        LocalDate today = LocalDate.now();

        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        int count = 0;
        int operationalCount = 0;
        int maintenanceCount = 0;
        int outOfServiceCount = 0;
        int issueCount = 0;

        for (LocalDate date : tripDates) {
            String dateStr = date.toString();
            Set<UUID> usedBusesOnDate = busesUsedByDate.getOrDefault(dateStr, new HashSet<>());
            boolean isWeekend = date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
                    date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;

            boolean isSpikePeriod = spikeDates.contains(dateStr);

            for (BusInfo bus : allBuses) {
                UUID busId = bus.id;
                BusOperationalStatus status;
                MaintenanceIssue issue = null;
                Priority priority = null;
                String description = null;
                LocalDate dateResolved = null;

                if (usedBusesOnDate.contains(busId)) {
                    status = BusOperationalStatus.OPERATIONAL;
                    operationalCount++;
                } else {
                    int previousIssues = busIssueFrequency.getOrDefault(busId, 0);
                    status = getRealisticStatus(date, isWeekend, bus.type, previousIssues, isSpikePeriod);

                    if (status == BusOperationalStatus.MAINTENANCE ||
                            status == BusOperationalStatus.OUT_OF_SERVICE) {

                        issue = getRealisticIssue(date, bus.type);
                        priority = getRealisticPriority(issue, status, isSpikePeriod);
                        description = getRealisticDescription(issue, bus.type, status);

                        // Resolution dates - OUT_OF_SERVICE takes longer
                        if (status == BusOperationalStatus.OUT_OF_SERVICE) {
                            int daysToResolve = ThreadLocalRandom.current().nextInt(3, 15);
                            dateResolved = date.plusDays(daysToResolve);
                            if (dateResolved.isAfter(today) || dateResolved.isAfter(tripDates.get(tripDates.size() - 1))) {
                                dateResolved = null;
                            }
                        } else {
                            int daysToResolve = ThreadLocalRandom.current().nextInt(1, 4);
                            dateResolved = date.plusDays(daysToResolve);
                            if (dateResolved.isAfter(today) || dateResolved.isAfter(tripDates.get(tripDates.size() - 1))) {
                                dateResolved = null;
                            }
                        }

                        busIssueFrequency.merge(busId, 1, Integer::sum);
                        issueCount++;
                    }

                    if (status == BusOperationalStatus.OPERATIONAL) operationalCount++;
                    else if (status == BusOperationalStatus.MAINTENANCE) maintenanceCount++;
                    else outOfServiceCount++;
                }

                batch.add(new Object[]{
                        UUID.randomUUID(),
                        busId,
                        status.name(),
                        issue != null ? issue.name() : null,
                        priority != null ? priority.name() : null,
                        description,
                        dateResolved,
                        date
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
        log.info("   Total records inserted: {}", count);
        log.info("   Operational: {} ({}%)", operationalCount,
                String.format("%.1f", (double) operationalCount / count * 100));
        log.info("   Maintenance: {} ({}%)", maintenanceCount,
                String.format("%.1f", (double) maintenanceCount / count * 100));
        log.info("   Out of Service: {} ({}%)", outOfServiceCount,
                String.format("%.1f", (double) outOfServiceCount / count * 100));
        log.info("   Issues recorded: {}", issueCount);
        log.info("   Records per bus: {}", count / allBuses.size());
        log.info("✅ Inserted in {} seconds", (endTime - startTime) / 1000);
    }

    /**
     * Pre-calculate realistic contiguous spike periods
     */
    private void calculateSpikePeriods(List<LocalDate> allDates) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        spikeDates.clear();

        for (LocalDate date : allDates) {
            int month = date.getMonthValue();
            int day = date.getDayOfMonth();

            // RAINY SEASON: Entire June-July period is problematic
            if (month == 6 || month == 7) {
                // 60% of rainy season days are bad
                if (random.nextDouble() < 0.6) {
                    spikeDates.add(date.toString());
                }
                continue;
            }

            // HEAT WAVE: Mid-Jan to mid-Feb
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

            // POST-HOLIDAY: Jan 6-20 (after New Year)
            if (month == 1 && day >= 6 && day <= 20) {
                if (random.nextDouble() < 0.5) {
                    spikeDates.add(date.toString());
                }
                continue;
            }

            // POST-EASTER: April 15-25
            if (month == 4 && day >= 15 && day <= 25) {
                if (random.nextDouble() < 0.5) {
                    spikeDates.add(date.toString());
                }
                continue;
            }

            // POST-SUMMER BREAK: September 1-15
            if (month == 9 && day >= 1 && day <= 15) {
                if (random.nextDouble() < 0.5) {
                    spikeDates.add(date.toString());
                }
                continue;
            }

            // WINTER COLD: July-August (cold start issues)
            if (month == 7 || month == 8) {
                if (random.nextDouble() < 0.2) {
                    spikeDates.add(date.toString());
                }
                continue;
            }

            // Random mini-spikes (5% of remaining days)
            if (random.nextDouble() < 0.05) {
                spikeDates.add(date.toString());
            }
        }

        log.info("   Spike days: {} ({}% of total)",
                spikeDates.size(),
                String.format("%.1f", (double) spikeDates.size() / allDates.size() * 100));
    }

    private BusOperationalStatus getRealisticStatus(
            LocalDate date,
            boolean isWeekend,
            String busType,
            int previousIssues,
            boolean isSpikePeriod) {

        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Electric buses are more reliable
        double baseReliability = "ELECTRIC".equals(busType) ? 0.88 : 0.75;

        // Problem bus penalty (escalating)
        if (previousIssues > 8) {
            baseReliability -= 0.25; // Chronic problem bus
        } else if (previousIssues > 5) {
            baseReliability -= 0.18;
        } else if (previousIssues > 3) {
            baseReliability -= 0.10;
        } else if (previousIssues > 1) {
            baseReliability -= 0.05;
        }

        // Weekend = more maintenance scheduled
        if (isWeekend) {
            baseReliability -= 0.10;
        }

        // Spike period = more breakdowns
        if (isSpikePeriod) {
            baseReliability -= 0.18; // Increased from 0.15 for more impact
        }

        // Random variation
        baseReliability += (random.nextDouble() - 0.5) * 0.08;
        baseReliability = Math.max(0.25, Math.min(0.95, baseReliability));

        int rand = random.nextInt(100);
        int threshold = (int) (baseReliability * 100);

        if (rand < threshold) {
            return BusOperationalStatus.OPERATIONAL;
        } else if (rand < threshold + 12) {
            return BusOperationalStatus.MAINTENANCE;
        } else {
            return BusOperationalStatus.OUT_OF_SERVICE;
        }
    }

    private MaintenanceIssue getRealisticIssue(LocalDate date, String busType) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int rand = random.nextInt(100);

        boolean isRainy = date.getMonthValue() == 6 || date.getMonthValue() == 7;
        boolean isHot = date.getMonthValue() == 1 || date.getMonthValue() == 2;

        // Electric buses have different issue profiles
        if ("ELECTRIC".equals(busType)) {
            if (isRainy) {
                if (rand < 50) return MaintenanceIssue.ELECTRICAL;
                else if (rand < 75) return MaintenanceIssue.TIRES;
                else if (rand < 90) return MaintenanceIssue.BODY;
                else return MaintenanceIssue.OTHER;
            } else if (isHot) {
                if (rand < 45) return MaintenanceIssue.ELECTRICAL;
                else if (rand < 70) return MaintenanceIssue.TIRES;
                else if (rand < 85) return MaintenanceIssue.BODY;
                else return MaintenanceIssue.OTHER;
            } else {
                if (rand < 40) return MaintenanceIssue.ELECTRICAL;
                else if (rand < 65) return MaintenanceIssue.TIRES;
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

    private Priority getRealisticPriority(MaintenanceIssue issue, BusOperationalStatus status, boolean isSpikePeriod) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        if (status == BusOperationalStatus.OUT_OF_SERVICE) {
            return random.nextDouble() < 0.7 ? Priority.CRITICAL : Priority.MAJOR;
        }

        if (issue == MaintenanceIssue.ENGINE) {
            if (random.nextDouble() < 0.5) return Priority.CRITICAL;
            else if (random.nextDouble() < 0.8) return Priority.MAJOR;
            else return Priority.MINOR;
        }

        if (issue == MaintenanceIssue.ELECTRICAL) {
            if (random.nextDouble() < 0.3) return Priority.CRITICAL;
            else if (random.nextDouble() < 0.6) return Priority.MAJOR;
            else return Priority.MINOR;
        }

        if (isSpikePeriod) {
            if (random.nextDouble() < 0.4) return Priority.CRITICAL;
            else if (random.nextDouble() < 0.7) return Priority.MAJOR;
            else return Priority.MINOR;
        }

        int rand = random.nextInt(100);
        if (rand < 20) return Priority.CRITICAL;
        else if (rand < 50) return Priority.MAJOR;
        else return Priority.MINOR;
    }

    private String getRealisticDescription(MaintenanceIssue issue, String busType, BusOperationalStatus status) {
        List<String> descriptions = new ArrayList<>();

        switch (issue) {
            case ENGINE -> {
                if ("ELECTRIC".equals(busType)) {
                    descriptions.addAll(Arrays.asList(
                            "Motor is hotter than a summer sidewalk - needs ice pack",
                            "Battery cooling system is on strike - refuses to work",
                            "Inverter threw a tantrum - power loss detected",
                            "Regenerative brakes decided to take a day off",
                            "Motor controller needs therapy - recalibration required"
                    ));
                } else {
                    descriptions.addAll(Arrays.asList(
                            "Engine overheating - it's having a fever dream",
                            "Engine making noises like a dying whale - please investigate",
                            "Engine said 'nope' and refused to start this morning",
                            "Oil leak - bus is marking its territory like a dog",
                            "Engine misfiring - it's having an identity crisis",
                            "Check engine light is having a rave party on my dashboard",
                            "Engine lost its mojo - fuel system issue",
                            "Timing belt is older than my grandpa - needs replacement"
                    ));
                }
            }
            case ELECTRICAL -> {
                if ("ELECTRIC".equals(busType)) {
                    descriptions.addAll(Arrays.asList(
                            "Battery pack is moody - cell balancing needed",
                            "Charging system is slower than a snail on vacation",
                            "HV battery warning light is bullying me",
                            "Battery management system needs a pep talk - error detected",
                            "Range dropped faster than my motivation on Monday morning"
                    ));
                } else {
                    descriptions.addAll(Arrays.asList(
                            "Battery dies faster than my phone battery",
                            "Alternator gave up on life - bus died mid-route",
                            "Lights have performance anxiety - keep flickering",
                            "Dashboard looks like a Christmas tree - too many lights",
                            "Starter motor said 'not today' - won't crank",
                            "Electrical short - bus is trying to become a fireworks display",
                            "Wiring harness is having a bad hair day"
                    ));
                }
            }
            case BODY -> descriptions.addAll(Arrays.asList(
                    "Door jammed - passengers staging a rebellion",
                    "Windshield cracked - looks like a spiderweb art project",
                    "Side mirror is hanging on for dear life",
                    "Seat has more cracks than my phone screen",
                    "Floorpan is rustier than my high school locker",
                    "Roof panel dented - someone didn't check the height clearance",
                    "Door latch is on strike - refuses to close properly"
            ));
            case TIRES -> descriptions.addAll(Arrays.asList(
                    "Tire went flat - it's tired of working",
                    "Tread is balder than my uncle - needs replacement",
                    "Tire had a blowout - dramatic exit from service",
                    "Tire wear is uneven - alignment has commitment issues",
                    "Valve stem leaking - it's a slow-motion disaster",
                    "TPMS is screaming at me - something's wrong"
            ));
            default -> descriptions.addAll(Arrays.asList(
                    "Suspension feels like riding a roller coaster - but not the fun kind",
                    "Brake pads are wearing thin - praying to stop in time",
                    "Transmission is slipping - gear changes are a surprise",
                    "Steering wheel shaking - bus wants to dance",
                    "Exhaust sounds like a dying lawnmower",
                    "AC is on strike - passengers are melting",
                    "Fuel gauge is lying to me - says I'm empty when I'm not",
                    "GPS is lost - probably looking for itself"
            ));
        }

        String base = descriptions.get(ThreadLocalRandom.current().nextInt(descriptions.size()));

        if (status == BusOperationalStatus.OUT_OF_SERVICE) {
            return base + " - BUS SAID 'I QUIT'";
        } else if (status == BusOperationalStatus.MAINTENANCE) {
            return base + " - Bus needs a spa day";
        }
        return base;
    }

    private record BusInfo(UUID id, String type) {}
}