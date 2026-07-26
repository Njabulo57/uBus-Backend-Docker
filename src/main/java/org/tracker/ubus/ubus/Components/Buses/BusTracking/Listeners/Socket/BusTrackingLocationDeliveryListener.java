package org.tracker.ubus.ubus.Components.Buses.BusTracking.Listeners.Socket;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Responses.DriverCurrentLocationResponse;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Event.Socket.BusTrackingLocationDeliveryEvent;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;
import java.util.stream.Stream;

@Slf4j
@Component
public class BusTrackingLocationDeliveryListener extends AbstractSocketListener<DriverCurrentLocationResponse> {


    public BusTrackingLocationDeliveryListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate, "/topic/admins/bus-tracking-location-delivery/");
    }

    @Async
    @EventListener
    public void onLocationReceived(BusTrackingLocationDeliveryEvent event) {

       log.info("Received BusTrackingLocationDeliveryEvent {}", event);
       log.info("Sending is done by Thread {}", Thread.currentThread());


        var driverCurrentLocations = event.getLocations();

        Stream.of(driverCurrentLocations)
                .forEach(location -> {
                    var route = location.route();
                    var tripId = location.tripId();

                    //let all admins know where the bus is
                    this.sendMessage("all-buses", location);
                    this.sendMessage("route/" + route +'/' + tripId , location);
                    this.sendMessage("trip/" + tripId , location);

                });

        //log.info("Sending was accomplished by Thread {}", Thread.currentThread());
    }

}
