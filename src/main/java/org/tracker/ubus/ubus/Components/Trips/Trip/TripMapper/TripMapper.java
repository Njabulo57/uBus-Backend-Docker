package org.tracker.ubus.ubus.Components.Trips.Trip.TripMapper;

import org.springframework.stereotype.Component;

import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.*;

import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;

import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLeg;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLegBusAssignment;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;


import java.time.LocalDate;
import java.util.*;

@Component
public class TripMapper {


    public Trip toEntity(BusAssignment busAssignment, Route route,
                         ScheduleLegBusAssignment scheduleLegBusAssignment) {

        var scheduleLeg = scheduleLegBusAssignment.getScheduleLeg();
        var today = LocalDate.now();
        var departureTime = scheduleLeg.getDepartureTime();
        return Trip.builder()
                .route(route)
                .departureTime(departureTime.atDate(today))
                .scheduleLegBusAssignment(scheduleLegBusAssignment)
                .isFromSimulation(false)
                .status(TripStatus.CREATED)
                .busAssignment(busAssignment)
                .totalCount(0)
                .build();
    }
}