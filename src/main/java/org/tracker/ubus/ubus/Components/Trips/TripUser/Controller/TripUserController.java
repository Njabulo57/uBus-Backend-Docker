package org.tracker.ubus.ubus.Components.Trips.TripUser.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Service.TripUserService;
import org.tracker.ubus.ubus.Components.Trips.TripUser.DTOs.Response.OnTripResponse;

@RestController
@RequiredArgsConstructor
public class TripUserController {


    private final TripUserService tripUserService;


    @PostMapping("/trip-user/is-on-trip")
    public OnTripResponse isOnTrip(){
        return this.tripUserService.isUserCurrentlyOnTrip();
    }
}
