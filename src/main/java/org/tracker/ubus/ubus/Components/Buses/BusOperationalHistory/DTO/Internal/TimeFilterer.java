package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Internal;


import java.time.LocalDate;

public record TimeFilterer(LocalDate start, LocalDate end) {

    public TimeFilterer {
        if (start.isAfter(end))
            throw new IllegalArgumentException("Start date cannot be after end date");
        var today = LocalDate.now();
        // if start is today or before, set it to today
        if(end.isAfter(today))
            end = today;
    }

    public static TimeFilterer of(LocalDate start, LocalDate end) {
        return new TimeFilterer(start, end);
    }

}
