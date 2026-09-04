package org.tracker.ubus.ubus.Components.Trips.Trip.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripEndRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripRegisterCoordinates;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripStartRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripTapRequest;
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
    public UUID registerTrip(@RequestBody TripRegisterCoordinates tripRegisterCoordinates) {
        return this.tripService.registerTrip(tripRegisterCoordinates);
    }


    @PostMapping("/start-trip")
    public void startTrip(@RequestBody TripStartRequest tripStartRequest) {
        this.tripService.startTrip(tripStartRequest);
    }


    @PostMapping("/end-trip")
    public void endTrip(@RequestBody TripEndRequest endRequest) {
        this.tripService.endTrip(endRequest);
    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/handle-tap/{tripId}")
    public int handleNfcTap(@PathVariable final UUID tripId, @RequestBody TripTapRequest nfcCode) {
        return this.tripService.handleNfcTap(tripId, nfcCode.nfcCode());
    }
}
