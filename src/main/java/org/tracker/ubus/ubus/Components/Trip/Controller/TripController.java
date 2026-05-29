package org.tracker.ubus.ubus.Components.Trip.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Trip.DTO.Request.TripRegisterRequest;
import org.tracker.ubus.ubus.Components.Trip.DTO.Response.ActiveTripResponse;
import org.tracker.ubus.ubus.Components.Trip.Service.Interface.ITripService;

import javax.swing.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trips")
public class TripController {

    private final ITripService tripService;


    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping("/register-bus-trip")
    public void registerTrip(@RequestBody final TripRegisterRequest tripRegisterRequest) {
        this.tripService.registerTrip(tripRegisterRequest);
    }


    @GetMapping("/get-active-trip")
    public List<ActiveTripResponse> getActiveTrip() {
        return this.tripService.getActiveTrips();
    }

    @GetMapping("/get-trip/{tripId}")
    public ActiveTripResponse getActiveTrip(@PathVariable final UUID tripId) {
        return this.tripService.getActiveTrip(tripId);
    }
}
