package org.tracker.ubus.ubus.Components.Trip.TripMapper;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Trip.DTO.Request.TripRegisterRequest;
import org.tracker.ubus.ubus.Components.Trip.DTO.Response.ActiveTripResponse;
import org.tracker.ubus.ubus.Components.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trip.Enum.TripRoute;
import org.tracker.ubus.ubus.Components.Trip.Enum.TripStatus;

import java.util.List;

@Component
public class TripMapper {

    public Trip toEntity(TripRegisterRequest registerRequest, BusAssignment busAssignment) {
        var tripRoute = TripRoute.fromLabel(registerRequest.tripRoute());

        return Trip.builder()
                .route(tripRoute)
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

        return ActiveTripResponse.builder()
                .id(trip.getId())
                .route(trip.getRoute().getLabel())
                .busName(bus.getName())
                .busId(busId)
                .build();

    }
}
