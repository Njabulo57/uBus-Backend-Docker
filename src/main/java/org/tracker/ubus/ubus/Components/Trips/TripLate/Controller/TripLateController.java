package org.tracker.ubus.ubus.Components.Trips.TripLate.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripRegisterCoordinates;
import org.tracker.ubus.ubus.Components.Trips.Trip.Service.Interface.ITripService;
import org.tracker.ubus.ubus.Components.Trips.TripLate.DTO.Request.TripLateRequest;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Service.Interface.ITripLateService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tripLate")
public class TripLateController {

    private final ITripLateService tripLateService;


    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/set-trip-late-reason")
    public void registerTrip(@RequestBody TripLateRequest tripLateRequest) {
        this.tripLateService.updateTripLate(tripLateRequest);
    }
}