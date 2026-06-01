package org.tracker.ubus.ubus.Components.Trips.Trip.Service.Interface;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripEndRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripRegisterCoordinates;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.PastTrip.AbstractPastTrip;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.ActiveTripResponse;

import java.util.List;
import java.util.UUID;

public interface ITripService {


    void registerTrip(TripRegisterCoordinates tripRegisterCoordinates);

    void endTrip(TripEndRequest endTripRequest);


    Page<AbstractPastTrip> getPastTrips(Pageable pageable);

    List<ActiveTripResponse> getActiveTrips();

    ActiveTripResponse getActiveTrip(UUID tripId);
}
