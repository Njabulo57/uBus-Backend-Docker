package org.tracker.ubus.ubus.Components.Buses.BusTracking.Event;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class BusDisconnectEvent {


    @Async
    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        log.info("Bus Disconnected");
    }
}
