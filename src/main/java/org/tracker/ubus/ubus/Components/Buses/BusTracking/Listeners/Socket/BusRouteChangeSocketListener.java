package org.tracker.ubus.ubus.Components.Buses.BusTracking.Listeners.Socket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Event.Socket.BusRouteChangeEvent;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;

import java.util.Collection;

@Slf4j
@Component
public class BusRouteChangeSocketListener extends AbstractSocketListener<Collection<double[]>> {


    public BusRouteChangeSocketListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate,"/topic/bus-route-change/");
    }


    @Async
    @EventListener
    public void onBusRouteChange(BusRouteChangeEvent event) {
        var tripId = event.getTrip().getId();
        var coordinates = event.getCoordinates();

        this.sendMessage(tripId.toString() , coordinates);
    }
}
