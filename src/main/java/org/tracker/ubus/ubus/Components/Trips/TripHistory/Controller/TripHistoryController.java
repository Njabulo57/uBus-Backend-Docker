package org.tracker.ubus.ubus.Components.Trips.TripHistory.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.Service.Interface.ITripHistoryService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/trips-history")
public class TripHistoryController {


    private final ITripHistoryService tripHistoryService;

    @RequestMapping("/get-past-trips")
    public Object getPastTrips(
            @PageableDefault(size = 15) Pageable pageable) {
        return this.tripHistoryService.getPastTrips(pageable);
    }

}
