package org.tracker.ubus.ubus.Components.Trip.TripMapper;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.BusRoute.Entity.BusRoute;
import org.tracker.ubus.ubus.Components.Trip.DTO.Response.ActiveTripResponse;
import org.tracker.ubus.ubus.Components.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.util.List;

@Component
public class TripMapper {

    public Trip toEntity(BusRoute busRoute, BusAssignment busAssignment, Route route) {

        return Trip.builder()
                .route(route)
                .status(TripStatus.IN_PROGRESS)
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
        var route = trip.getRoute();

        var busStatus = bus.getActivityStatus().getLabel();
        return ActiveTripResponse.builder()
                .id(trip.getId())
                .route(route.getLabel())
                .busName(bus.getName())
                .busStatus(busStatus)
                .busId(busId)
                .build();

    }
}
