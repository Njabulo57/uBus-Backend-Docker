package org.tracker.ubus.ubus.Components.Trips.Trip.Service.Interface;


import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripEndRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripRegisterCoordinates;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripStartRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.ActiveTripResponse;

import java.util.List;
import java.util.UUID;

public interface ITripService {

    void startTrip(TripStartRequest tripStartRequest);

    UUID registerTrip(TripRegisterCoordinates tripRegisterCoordinates);

    void endTrip(TripEndRequest endTripRequest);

    int handleNfcTap(UUID tripId, String nfcCode);


    List<ActiveTripResponse> getActiveTrips();

    ActiveTripResponse getActiveTrip(UUID tripId);
}
