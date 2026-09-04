package org.tracker.ubus.ubus.TestDataLoader.WorkAttendance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Attendence.Entity.WorkAttendance;
import org.tracker.ubus.ubus.Components.Attendence.Repository.WorkAttendanceRepository;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Order(8)
@Component
@RequiredArgsConstructor
public class WorkAttendanceGenerator implements CommandLineRunner {

    private final WorkAttendanceRepository workAttendanceRepository;
    private final UserRepository userRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final JdbcTemplate jdbcTemplate;

    // Date range: 14 February 2026 to Today
    private static final LocalDate YEAR_START = LocalDate.of(2026, 2, 14);
    private static final LocalDate YEAR_END = LocalDate.now();

    @Override
    public void run(String... args) throws Exception {
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║     WORK ATTENDANCE GENERATOR                              ║");
        log.info("║     Date Range: {} to {}                                   ║", YEAR_START, YEAR_END);
        log.info("╚════════════════════════════════════════════════════════════╝");
        // Uncomment to run:
        //fastDelete();
        //generateAttendance();
    }

    private void fastDelete() {
        log.info("🗑️  Deleting existing work attendance...");
        jdbcTemplate.execute("TRUNCATE TABLE work_attendance CASCADE");
        log.info("✅ Deleted");
    }

    private void generateAttendance() {
        long startTime = System.currentTimeMillis();

        Long existingCount = workAttendanceRepository.count();
        if (existingCount > 0) {
            log.info("✅ Already {} attendance records exist. Skipping.", existingCount);
            return;
        }

        // Get all active drivers
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        if (drivers.isEmpty()) {
            log.error("❌ No drivers found!");
            return;
        }

        log.info("📊 Found {} drivers", drivers.size());

        // Get all bus assignments to know which drivers are assigned to which buses
        List<BusAssignment> busAssignments = busAssignmentRepository.findAll();

        // Map driver ID to their bus assignment
        Map<UUID, BusAssignment> driverBusMap = new HashMap<>();
        for (BusAssignment assignment : busAssignments) {
            driverBusMap.put(assignment.getDriver().getId(), assignment);
        }

        log.info("📊 Found {} bus assignments", busAssignments.size());

        // Dynamically assign driver profiles based on their assignment count
        Map<UUID, DriverProfile> driverProfiles = new HashMap<>();
        for (User driver : drivers) {
            double random = ThreadLocalRandom.current().nextDouble();
            DriverProfile profile;

            // Randomly assign profiles - 20% perfect, 20% sickly, 60% normal
            if (random < 0.20) {
                profile = new DriverProfile(
                        0.98, // 98% attendance
                        0.10, // 10% sick rate
                        "PERFECT"
                );
            } else if (random < 0.40) {
                profile = new DriverProfile(
                        0.85, // 85% attendance
                        0.60, // 60% sick rate
                        "SICKLY"
                );
            } else {
                profile = new DriverProfile(
                        0.95, // 95% attendance
                        0.30, // 30% sick rate
                        "NORMAL"
                );
            }

            driverProfiles.put(driver.getId(), profile);
        }

        // Generate all dates from YEAR_START to YEAR_END
        List<LocalDate> allDates = new ArrayList<>();
        LocalDate date = YEAR_START;
        while (!date.isAfter(YEAR_END)) {
            allDates.add(date);
            date = date.plusDays(1);
        }

        log.info("📊 Generating attendance for {} days ({} to {})",
                allDates.size(), allDates.get(0), allDates.get(allDates.size() - 1));

        List<WorkAttendance> attendanceRecords = new ArrayList<>();
        int totalWorkDays = 0;
        int presentCount = 0;
        int absentCount = 0;
        int sickCount = 0;

        Map<UUID, Integer> driverAbsences = new HashMap<>();
        Map<UUID, Integer> consecutiveAbsences = new HashMap<>();

        for (LocalDate currentDate : allDates) {
            boolean isWeekend = currentDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
                    currentDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
            boolean isHoliday = isHoliday(currentDate);
            boolean isSpikePeriod = isSpikePeriod(currentDate);

            for (User driver : drivers) {
                BusAssignment assignment = driverBusMap.get(driver.getId());

                // If driver has no bus assignment, they can't work
                if (assignment == null) {
                    continue;
                }

                totalWorkDays++;
                DriverProfile profile = driverProfiles.get(driver.getId());

                // Determine attendance
                boolean isPresent = determineAttendance(
                        currentDate,
                        profile,
                        isWeekend,
                        isHoliday,
                        isSpikePeriod,
                        consecutiveAbsences.getOrDefault(driver.getId(), 0)
                );

                if (isPresent) {
                    // Driver is present - create attendance record
                    LocalDateTime signedAt = generateSignInTime(currentDate, isWeekend, isHoliday);

                    WorkAttendance attendance = WorkAttendance.builder()
                            .driver(driver)
                            .signedAt(signedAt)
                            .build();

                    attendanceRecords.add(attendance);
                    presentCount++;

                    // Reset consecutive absences
                    consecutiveAbsences.put(driver.getId(), 0);
                } else {
                    // Driver is absent
                    absentCount++;
                    driverAbsences.merge(driver.getId(), 1, Integer::sum);
                    consecutiveAbsences.merge(driver.getId(), 1, Integer::sum);

                    // Check if sick
                    if (isSickDay(profile, isSpikePeriod)) {
                        sickCount++;
                    }
                }

                // Batch save every 1000 records
                if (attendanceRecords.size() >= 1000) {
                    workAttendanceRepository.saveAll(attendanceRecords);
                    attendanceRecords.clear();
                    log.info("   📝 Saved {} attendance records...", presentCount);
                }
            }
        }

        // Save remaining records
        if (!attendanceRecords.isEmpty()) {
            workAttendanceRepository.saveAll(attendanceRecords);
        }

        long endTime = System.currentTimeMillis();
        log.info("");
        log.info("📊 Summary Statistics:");
        log.info("   Date Range: {} to {}", YEAR_START, YEAR_END);
        log.info("   Total days: {}", allDates.size());
        log.info("   Total drivers: {}", drivers.size());
        log.info("   Total work days: {}", totalWorkDays);
        log.info("   Present: {} ({}%)", presentCount,
                String.format("%.1f", totalWorkDays > 0 ? (double) presentCount / totalWorkDays * 100 : 0));
        log.info("   Absent: {} ({}%)", absentCount,
                String.format("%.1f", totalWorkDays > 0 ? (double) absentCount / totalWorkDays * 100 : 0));
        log.info("   Sick days: {} ({}% of absences)", sickCount,
                String.format("%.1f", absentCount > 0 ? (double) sickCount / absentCount * 100 : 0));
        log.info("✅ Inserted in {} seconds", (endTime - startTime) / 1000);

        logAttendanceSummary(drivers, driverAbsences, driverProfiles, allDates.size());
    }

    private boolean determineAttendance(
            LocalDate date,
            DriverProfile profile,
            boolean isWeekend,
            boolean isHoliday,
            boolean isSpikePeriod,
            int consecutiveAbsences) {

        ThreadLocalRandom random = ThreadLocalRandom.current();

        double attendanceRate = profile.attendanceRate;

        // Weekend - slightly more likely to be absent
        if (isWeekend) {
            attendanceRate -= 0.05;
        }

        // Holiday - more likely to be absent
        if (isHoliday) {
            attendanceRate -= 0.10;
        }

        // Spike period - more absences (like flu season)
        if (isSpikePeriod) {
            attendanceRate -= 0.08;
        }

        // Consecutive absences - if already absent, more likely to be absent again
        if (consecutiveAbsences > 2) {
            attendanceRate -= 0.10;
        } else if (consecutiveAbsences > 1) {
            attendanceRate -= 0.05;
        }

        // Random variation
        attendanceRate += (random.nextDouble() - 0.5) * 0.05;
        attendanceRate = Math.max(0.70, Math.min(0.99, attendanceRate));

        return random.nextDouble() < attendanceRate;
    }

    private boolean isSickDay(DriverProfile profile, boolean isSpikePeriod) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        double sickRate = profile.sickRate;

        // Spike period (flu season) - more sick days
        if (isSpikePeriod) {
            sickRate += 0.20;
        }

        // Random variation
        sickRate += (random.nextDouble() - 0.5) * 0.10;
        sickRate = Math.max(0.05, Math.min(0.70, sickRate));

        return random.nextDouble() < sickRate;
    }

    private LocalDateTime generateSignInTime(LocalDate date, boolean isWeekend, boolean isHoliday) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Base sign-in time - between 6:00 and 8:30 on weekdays
        int hour;
        int minute;

        if (isWeekend || isHoliday) {
            // Weekend/holiday - later start
            hour = 7 + random.nextInt(3); // 7:00 - 9:30
            minute = random.nextInt(60);
        } else {
            // Weekday - normal start
            hour = 6 + random.nextInt(3); // 6:00 - 8:30
            minute = random.nextInt(60);
        }

        // Some drivers are always early
        if (random.nextDouble() < 0.2) {
            hour = Math.max(5, hour - 1); // Early birds
        }

        // Some drivers are always late
        if (random.nextDouble() < 0.1) {
            hour = Math.min(10, hour + 1); // Late risers
        }

        return LocalDateTime.of(date, LocalTime.of(hour, minute));
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

    private boolean isSpikePeriod(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        // Flu season (winter)
        if (month == 6 || month == 7) return true;

        // Post-holiday (January)
        if (month == 1 && day >= 6 && day <= 20) return true;

        // Post-Easter
        if (month == 4 && day >= 15 && day <= 25) return true;

        // Post-summer break
        if (month == 9 && day >= 1 && day <= 15) return true;

        return false;
    }

    private void logAttendanceSummary(List<User> drivers, Map<UUID, Integer> driverAbsences,
                                      Map<UUID, DriverProfile> driverProfiles, int totalDays) {
        log.info("");
        log.info("📊 Driver Attendance Summary:");
        log.info("   {:30} {:15} {:12} {:10} {:10}", "Driver", "Attendance Rate", "Absences", "Profile", "Status");
        log.info("   {}", "-".repeat(80));

        List<User> sortedDrivers = new ArrayList<>(drivers);
        sortedDrivers.sort((a, b) -> {
            int absA = driverAbsences.getOrDefault(a.getId(), 0);
            int absB = driverAbsences.getOrDefault(b.getId(), 0);
            return Integer.compare(absB, absA);
        });

        for (User driver : sortedDrivers) {
            String driverName = driver.getFirstname() + " " + driver.getLastname();
            int absences = driverAbsences.getOrDefault(driver.getId(), 0);
            double attendanceRate = totalDays > 0 ? (1 - (double) absences / totalDays) * 100 : 100;

            DriverProfile profile = driverProfiles.get(driver.getId());
            String profileLabel = profile != null ? profile.label : "NORMAL";

            String status;
            String emoji;
            if (attendanceRate > 95) {
                status = "Perfect";
                emoji = "✅";
            } else if (attendanceRate > 85) {
                status = "Good";
                emoji = "👍";
            } else if (attendanceRate > 75) {
                status = "Average";
                emoji = "⚠️";
            } else {
                status = "Poor";
                emoji = "❌";
            }

            log.info("   {} {}% {} {} {} {}",
                    driverName,
                    attendanceRate,
                    absences,
                    profileLabel,
                    status,
                    emoji);
        }

        log.info("");
        log.info("   Legend:");
        log.info("   ✅ - Perfect attendance (>95%)");
        log.info("   👍 - Good attendance (85-95%)");
        log.info("   ⚠️ - Average attendance (75-85%)");
        log.info("   ❌ - Poor attendance (<75%)");
        log.info("");
        log.info("   Profiles:");
        log.info("   PERFECT - 98% attendance, rarely sick");
        log.info("   NORMAL - 95% attendance, occasional sick");
        log.info("   SICKLY - 85% attendance, frequently sick");
    }

    private static class DriverProfile {
        final double attendanceRate;
        final double sickRate;
        final String label;

        DriverProfile(double attendanceRate, double sickRate, String label) {
            this.attendanceRate = attendanceRate;
            this.sickRate = sickRate;
            this.label = label;
        }
    }
}