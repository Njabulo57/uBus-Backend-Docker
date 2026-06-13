package org.tracker.ubus.ubus.Components.Trips.Trip.Service.Interface;

import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripEndRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripRegisterCoordinates;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.ActiveTripResponse;

import java.util.List;
import java.util.UUID;

public interface ITripService {


    void registerTrip(TripRegisterCoordinates tripRegisterCoordinates);

    void endTrip(TripEndRequest endTripRequest);



    List<ActiveTripResponse> getActiveTrips();

    ActiveTripResponse getActiveTrip(UUID tripId);
}
