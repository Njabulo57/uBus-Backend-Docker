package org.tracker.ubus.ubus.Components.Trips.Trip.Service.Interface;

import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripEndRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripRegisterCoordinates;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.ActiveTripResponse;

import java.util.List;
import java.util.UUID;

public interface ITripService {

    void startTrip(UUID tripId);

    void registerTrip(TripRegisterCoordinates tripRegisterCoordinates);

    void endTrip(TripEndRequest endTripRequest);

    void enterBus(UUID tripId);

    void exitBus(UUID tripId);

    List<ActiveTripResponse> getActiveTrips();

    ActiveTripResponse getActiveTrip(UUID tripId);
}
