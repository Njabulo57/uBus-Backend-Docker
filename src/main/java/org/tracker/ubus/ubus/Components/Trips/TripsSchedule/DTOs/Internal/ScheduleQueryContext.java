package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Internal;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record ScheduleQueryContext(Bus bus, LocalTime startTime,
                                   LocalTime endTime, LocalDate currentDate) {

    public ScheduleQueryContext {
        if(startTime.isAfter(endTime))
            throw new IllegalStateException("Start Time Cannot Be After End Time");
    }
}
