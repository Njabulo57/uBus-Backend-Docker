package org.tracker.ubus.ubus.Components.Trips.Trip.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripEndRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripRegisterCoordinates;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.ActiveTripResponse;
import org.tracker.ubus.ubus.Components.Trips.Trip.Service.Interface.ITripService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trips")
public class TripController {

    private final ITripService tripService;


    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping("/register-bus-trip")
    public void registerTrip(@RequestBody TripRegisterCoordinates tripRegisterCoordinates) {
        this.tripService.registerTrip(tripRegisterCoordinates);
    }


    @RequestMapping("/end-trip")
    public void endTrip(@RequestBody TripEndRequest endRequest) {
        this.tripService.endTrip(endRequest);
    }



    @GetMapping("/get-active-trips")
    public List<ActiveTripResponse> getActiveTrips() {
        return this.tripService.getActiveTrips();
    }





    @GetMapping("/get-trip/{tripId}")
    public ActiveTripResponse getActiveTrip(@PathVariable final UUID tripId) {
        return this.tripService.getActiveTrip(tripId);
    }
}
