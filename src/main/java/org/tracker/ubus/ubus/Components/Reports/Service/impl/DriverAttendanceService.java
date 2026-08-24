package org.tracker.ubus.ubus.Components.Reports.Service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Attendence.Repository.WorkAttendanceRepository;
import org.tracker.ubus.ubus.Components.Reports.Abstract.AbstractReportService;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.DriverAttendanceResponse;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.DriverAttendanceWrapper;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleIncludedRepository;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverAttendanceService extends AbstractReportService {

    private final TripRepository tripRepository;
    private final WorkAttendanceRepository workAttendanceRepository;
    private final UserRepository userRepository;
    private final ScheduleIncludedRepository scheduleDatesExcludedRepository;

    public DriverAttendanceWrapper getDriversAttendance(ReportsTime reportsTime) {


        var strtPerformanceTime = System.currentTimeMillis();

        var role = UserRole.DRIVER;
        var status = UserStatus.ACTIVE;
        var allDrivers = this.userRepository.findByRoleAndStatus(role, status);


        var startDate = this.getTimeRangeByReportingFilter(reportsTime);
        var endDate = LocalDate.now();
        var endOfDay = getEndOfDayDateTime(endDate);
        var startOfDay = startDate.toLocalDate()
                .atStartOfDay();

        // Get all attendance records in range
        var attendanceRecords = this.workAttendanceRepository
                .findSignedAtBetween(startOfDay, endOfDay);


        // Get all trips in range
        var tripsInRange = this.tripRepository
                .findByCreatedAtBetween(startOfDay, endOfDay);

        var tripQueryEndTime = System.currentTimeMillis();
        log.info("Time taken to fetch trips from database: {} seconds", (tripQueryEndTime - strtPerformanceTime) / 1000.0);

        var dateOfRange = startDate.toLocalDate();
        // Get excluded dates (holidays, etc.) within the range
        var excludedDates = this.scheduleDatesExcludedRepository
                .findByExcludedDateBetween(dateOfRange, endDate)
                .stream()
                .map(sde -> {
                    // Collect all dates between fromDate and toDate
                    var dates = new ArrayList<LocalDate>();
                    var current = sde.getFromDate();
                    while (!current.isAfter(sde.getToDate())) {
                        dates.add(current);
                        current = current.plusDays(1);
                    }
                    return dates;
                })
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        var dbEndTime = System.currentTimeMillis();
        var dbPerformanceTime = (dbEndTime - strtPerformanceTime) / 1000.0;
        log.info("Time taken to fetch data from database: {} seconds", dbPerformanceTime);

        // Get working days in range (excluding weekends AND excluded dates)
        var workingDates = getWorkingDaysInRangeExcludingDates(dateOfRange, endDate, excludedDates);

        // Group attendance by driver and date
        var attendanceByDriverAndDate = attendanceRecords.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getDriver().getId(),
                        Collectors.mapping(
                                record -> record.getSignedAt().toLocalDate(),
                                Collectors.toSet()
                        )
                ));

        // Group trips by driver
        var tripsByDriver = tripsInRange.stream()
                .filter(trip -> trip.getBusAssignment() != null)
                .filter(trip -> trip.getBusAssignment().getDriver() != null)
                .collect(Collectors.groupingBy(
                        trip -> trip.getBusAssignment().getDriver().getId(),
                        Collectors.counting()
                ));


        var driverAttendances = allDrivers.stream()
                .map(driver -> {
                    var driverId = driver.getId();
                    var attendanceDates = attendanceByDriverAndDate.getOrDefault(driverId, Set.of());
                    var tripCount = tripsByDriver.getOrDefault(driverId, 0L);

                    var daysPresent = workingDates.stream()
                            .filter(attendanceDates::contains)
                            .count();

                    var daysAbsent = workingDates.size() - daysPresent;

                    var tripsCompleted = Math.toIntExact(tripCount);
                    var percentage = calculatePercentage(daysPresent, workingDates.size());

                    var isFlagged = percentage < 75;

                    var driverName = formatName(driver);
                    return DriverAttendanceResponse.builder()
                            .name(driverName)
                            .totalWorkingDays(workingDates.size())
                            .daysPresent((int) daysPresent)
                            .daysAbsent((int) daysAbsent)
                            .tripsCompleted(tripsCompleted)
                            .isFlagged(isFlagged)
                            .attendancePercentage(percentage)
                            .build();
                })
                .sorted(Comparator.comparing(DriverAttendanceResponse::name))
                .toList();

        var endPerformanceTime = System.currentTimeMillis();
        var totalTimeInsecs = (strtPerformanceTime - endPerformanceTime) / 1000.0;
        log.info("Time taken to complete request {}", totalTimeInsecs);
        return DriverAttendanceWrapper.builder()
                .responses(driverAttendances)
                .build();
    }

    private List<LocalDate> getWorkingDaysInRangeExcludingDates(
            LocalDate startDate,
            LocalDate endDate,
            Set<LocalDate> excludedDates) {

        var workingDays = new java.util.ArrayList<LocalDate>();
        var current = startDate;

        while (!current.isAfter(endDate)) {
            var dayOfWeek = current.getDayOfWeek();
            boolean isWeekend = dayOfWeek == java.time.DayOfWeek.SATURDAY ||
                    dayOfWeek == java.time.DayOfWeek.SUNDAY;
            boolean isExcluded = excludedDates.contains(current);

            if (!isWeekend && !isExcluded) {
                workingDays.add(current);
            }
            current = current.plusDays(1);
        }

        return workingDays;
    }

    private double calculatePercentage(long count, long total) {
        if (total == 0) return 0.0;
        return Math.round((count * 100.0) / total * 10.0) / 10.0;
    }


    private LocalDateTime getEndOfDayDateTime(LocalDate date) {
        return date.atTime(23, 59, 59);
    }


    private String formatName(User user) {
        var firstChar = user.getFirstname()
                .charAt(0);
        return Character.toUpperCase(firstChar) + ". " + user.getLastname();
    }

}