package org.tracker.ubus.ubus.Components.BusTracking.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.tracker.ubus.ubus.Components.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.BusTracking.Service.Interface.IBusTrackingService;

import java.util.UUID;


@Slf4j
@Controller
@RequiredArgsConstructor
public class BusTrackingController {

    private final IBusTrackingService busTrackingService;
    private final SimpMessagingTemplate simpMessagingTemplate;





    @MessageMapping("/bus/get-location")
    public void busTracking(@Payload DriverCurrentLocationMessage location) {

        var response = this.busTrackingService.queueLocationForItsBatch(location);

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
