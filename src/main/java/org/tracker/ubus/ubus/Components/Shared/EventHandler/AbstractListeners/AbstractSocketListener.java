package org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;


@RequiredArgsConstructor
public abstract class AbstractSocketListener<T> {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final String defaultDestination;


    protected void sendMessage(String destination, T message) {
        simpMessagingTemplate.convertAndSend(this.defaultDestination + destination, message);
    }
}
