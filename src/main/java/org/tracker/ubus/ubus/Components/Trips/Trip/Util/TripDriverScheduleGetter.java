package org.tracker.ubus.ubus.Components.Trips.Trip.Util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleRepository;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class TripDriverScheduleGetter {

    private final ScheduleRepository shScheduleRepository;

    public Schedule getDriverCurrentScheduleOrThrow(BusAssignment busAssignment)
        throws IllegalStateException {

        var bus = busAssignment.getBus();
        var driverSchedule = busAssignment.getDriverSchedule();
        var startTime = driverSchedule.getStartTime();
        var endTime = driverSchedule.getEndTime();

        var today = LocalDate.now();
        return this.shScheduleRepository.findDriverCurrentScheduleTrip(bus, today,
                startTime, endTime)
                .orElseThrow(() -> new IllegalArgumentException("Next Trip Not Available"));
    }

}
