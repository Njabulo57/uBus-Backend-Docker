package org.tracker.ubus.ubus.Components.BusTracking.Mappers;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.BusTracking.DTO.Responses.DriverCurrentLocationResponse;
import org.tracker.ubus.ubus.Components.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.TripHistory.Entity.TripHistoryPoint;

import java.time.LocalTime;
import java.util.List;


@Component
public class BusTrackingMapper {


    public TripHistoryPoint toEntity(DriverCurrentLocationMessage location, Trip trip) {
        return TripHistoryPoint.builder()
                .latitude(location.latitude())
                .longitude(location.longitude())
                .speed(location.speed())
                .trip(trip)
                .timePosted(location.timePosted())
                .build();

    }

    public List<TripHistoryPoint> toEntities(List<DriverCurrentLocationMessage> locations, Trip trip) {
        return locations.stream()
                .map(locationMessage -> toEntity(locationMessage, trip))
                .toList();
    }

    public DriverCurrentLocationResponse toDTO(Trip trip) {

        var busAssignment = trip.getBusAssignment();
        var bus = busAssignment.getBus();

        var route = trip.getRoute().getLabel();

        return DriverCurrentLocationResponse.builder()
                .busId(bus.getId())
                .route(route)
                .eta(LocalTime.NOON) // this will come from the trip tracking subsystem
                .delay(null)
                .build();

    }



}
