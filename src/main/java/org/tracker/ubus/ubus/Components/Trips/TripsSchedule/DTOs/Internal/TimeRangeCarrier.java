package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Internal;

import java.time.DayOfWeek;
import java.time.LocalTime;


public record TimeRangeCarrier(LocalTime timeRange, DayOfWeek dayOfWeek) {

    public static TimeRangeCarrier of(LocalTime timeRange, DayOfWeek dayOfWeek) {
        return new TimeRangeCarrier(timeRange, dayOfWeek);
    }
}
