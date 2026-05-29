package org.tracker.ubus.ubus.Components.Trip.Service.Interface;

import org.tracker.ubus.ubus.Components.Trip.DTO.Request.TripRegisterRequest;
import org.tracker.ubus.ubus.Components.Trip.DTO.Response.ActiveTripResponse;

import java.util.List;
import java.util.UUID;

public interface ITripService {


    void registerTrip(TripRegisterRequest tripRegisterRequest);

    List<ActiveTripResponse> getActiveTrips();

    ActiveTripResponse getActiveTrip(UUID tripId);
}
