package org.tracker.ubus.ubus.Components.Trips.Trip.TripMapper;

import org.springframework.stereotype.Component;

import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.*;

import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;

import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;


import java.util.*;

@Component
public class TripMapper {


    public Trip toEntity(BusAssignment busAssignment, Route route, Schedule schedule) {
        return Trip.builder()
                .route(route)
                .schedule(schedule)
                .status(TripStatus.CREATED)
                .busAssignment(busAssignment)
                .totalCount(0)
                .build();
    }

    public List<ActiveTripResponse> toDTO(List<Trip> trips) {
        return trips.stream()
                .map(this::toDTO)
                .toList();
    }

    public ActiveTripResponse toDTO(Trip trip) {
        var busAssignment = trip.getBusAssignment();
        var bus = busAssignment.getBus();
        var busId = bus.getId();

        var driver = busAssignment.getDriver();
        var driverName = formatDriverName(driver);
        var busStatus = bus.getActivityStatus().getLabel();

        var schedule = trip.getSchedule();


        var builder = ActiveTripResponse.builder()
                .id(trip.getId())
                .route(schedule.getRoute().getLabel())
                .busName(bus.getName()) //
                .busStatus(busStatus)
                .busId(busId) //
                .driverName(driverName); //


        return toDTO(builder, schedule);
    }



    private ActiveTripResponse toDTO(ActiveTripResponse.ActiveTripResponseBuilder builder, Schedule schedule) {

        ActiveTripResponse activeTripResponse = null;
        if(schedule != null) {

            var fromDest = schedule.getFromDestination();
            var toDest = schedule.getToDestination();

            activeTripResponse = builder
                    .from(fromDest.name())
                    .to(toDest.name())

                    .fromLongitude(fromDest.getLng())
                    .fromLatitude(fromDest.getLat())

                    .toLongitude(toDest.getLng())
                    .toLatitude(toDest.getLat())
                    .build();
        }

        else
            activeTripResponse = builder.build();

        return activeTripResponse;
    }

    private String formatDriverName(User user) {
        var firstName = user.getFirstname();
        var firstNameInitialCapitalized = Character.toUpperCase(firstName.charAt(0));
        return firstNameInitialCapitalized + ". " + user.getLastname();
    }
}