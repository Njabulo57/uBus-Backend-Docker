package org.tracker.ubus.ubus.Components.SIMULATION;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripEndRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripRegisterCoordinates;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripStartRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Service.Impl.TripService;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;

@Component
@RequiredArgsConstructor
public class BusDispatchSimulator {



    private final TripService tripService;


    public void endTrip(Trip trip, double latitude, double longitude) {
        var tripId = trip.getId();
        this.tripService.endTrip(new TripEndRequest(tripId, latitude, longitude));
    }


    public void startTrip(Trip trip, double latitude, double longitude) {
        var tripId = trip.getId();
        this.tripService.startTrip(new TripStartRequest(latitude, longitude, tripId));
    }


    public void addNewTripIfExists(Schedule schedule) {

        var fromDestination = schedule.getFromDestination();

        var lat = fromDestination.getLat();
        var lon = fromDestination.getLng();

        this.tripService.registerTrip(new TripRegisterCoordinates(lat, lon));
    }

}
