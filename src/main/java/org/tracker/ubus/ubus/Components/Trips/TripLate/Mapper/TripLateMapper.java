package org.tracker.ubus.ubus.Components.Trips.TripLate.Mapper;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripLate.DTO.Request.TripLateRequest;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Entity.TripLate;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Enum.TripLateReason;

@Component
public class TripLateMapper {


    public TripLate toEntity(TripLateRequest tripLateRequest, Trip trip) {

        TripLate.TripLateBuilder tripLateBuilder;

        tripLateBuilder = TripLate.builder()
                    .trip(trip)
                    .reason(TripLateReason.valueOf(tripLateRequest.reason()));

        if (tripLateRequest.description() != null && !tripLateRequest.description().isBlank())
            tripLateBuilder.description(tripLateRequest.description());

        return tripLateBuilder
                .build();
    }
}
