package org.tracker.ubus.ubus.Components.BusTracking.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.tracker.ubus.ubus.Components.BusTracking.DTO.Requests.BusCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.BusTracking.Service.Interface.IBusTrackingService;

import java.util.UUID;


@Slf4j
@Controller
@RequiredArgsConstructor
public class BusTrackingController {

    private final IBusTrackingService busTrackingService;
    private final SimpMessagingTemplate simpMessagingTemplate;


    public Object trackAllBuses() {
        return null;
    }



    @MessageMapping("/bus/get-location")
    public void trackSpecificBus(@Payload BusCurrentLocationMessage location) {

        UUID busID = this.busTrackingService.queueLocationForItsBatch(location);

        //let all admins know where the bus is
        this.sendTo("/topic/admins/all-buses", location);


        //let all students listening to this request know
        this.sendTo("/topic/students/get-bus-location/" + busID, location);
    }



    private void sendTo(String destination, BusCurrentLocationMessage location) {
        this.simpMessagingTemplate.convertAndSend(destination, location);
    }

}
