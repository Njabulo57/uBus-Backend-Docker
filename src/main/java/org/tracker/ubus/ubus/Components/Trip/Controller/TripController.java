package org.tracker.ubus.ubus.Components.Trip.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Trip.DTO.Request.TripRegisterCoordinates;
import org.tracker.ubus.ubus.Components.Trip.DTO.Response.ActiveTripResponse;
import org.tracker.ubus.ubus.Components.Trip.Service.Interface.ITripService;

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


    @GetMapping("/get-active-trips")
    public List<ActiveTripResponse> getActiveTrip() {
        return this.tripService.getActiveTrips();
    }


    @GetMapping("get-/driver-assigned-trip")
    public ActiveTripResponse getDriverAssignedTrip() {
        return this.tripService.getBusAssignedTrip();
    }



    @GetMapping("/get-trip/{tripId}")
    public ActiveTripResponse getActiveTrip(@PathVariable final UUID tripId) {
        return this.tripService.getActiveTrip(tripId);
    }
}
