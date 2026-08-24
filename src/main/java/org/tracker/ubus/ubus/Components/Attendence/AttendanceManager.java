package org.tracker.ubus.ubus.Components.Attendence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Attendence.Entity.WorkAttendance;
import org.tracker.ubus.ubus.Components.Attendence.Repository.WorkAttendanceRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AttendanceManager {

    private final WorkAttendanceRepository workAttendanceRepository;

    public void trySignAttendanceForDriver(Trip trip, User driver) {

        var today = LocalDate.now();
        var startOfDay = today.atStartOfDay();
        var endOfDay = getEndOfDayDateTime(today);

        boolean alreadySigned = this.workAttendanceRepository.existsByDriverAndSignedAtBetween(driver, startOfDay,
                endOfDay);

        if(alreadySigned) return;

        var workAttendance = createWorkAttendance(driver);
        this.workAttendanceRepository.save(workAttendance);
    }


    private LocalDateTime getEndOfDayDateTime(LocalDate date) {
        return date.atTime(23, 59, 59);
    }


    private WorkAttendance createWorkAttendance(User driver) {

        return WorkAttendance.builder()
                .driver(driver)
                .signedAt(LocalDateTime.now())
                .build();
    }

}
