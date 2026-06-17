package org.tracker.ubus.ubus.Components.Buses.BusTracking.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Service.Interface.IBusLocationBatchService;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripEndingRequest;

/**
 * The BusTrackingController class is responsible for handling real-time bus tracking operations
 * such as managing updates on bus locations and ending bus trips.
 * This controller communicates over WebSocket channels to notify clients about bus location updates.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class BusTrackingController {

    private final IBusLocationBatchService busTrackingService;
    private final SimpMessagingTemplate simpMessagingTemplate;


    @MessageMapping("/buses/-end-trip")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endTrip(@Payload TripEndingRequest tripEndingRequest) {
        this.busTrackingService.endTrip(tripEndingRequest.tripId());
    }


    @MessageMapping("/buses/get-location")

    public void busTracking(@Payload DriverCurrentLocationMessage location) {

        var response = this.busTrackingService.enqueue(location);

        var route = response.route();
        var busId = response.busId();


        //let all admins know where the bus is
        this.sendTo("/topic/admins/all-buses", location);


        this.sendTo("/topic/students/get-bus-location/" + busId, location);
        this.sendTo("/topic/studnets/on-board/" + busId, location);


        this.sendTo("/topic/route-tracking/" + route, location);
    }


    private void sendTo(String destination, DriverCurrentLocationMessage location) {
        this.simpMessagingTemplate.convertAndSend(destination, location);
    }

}
