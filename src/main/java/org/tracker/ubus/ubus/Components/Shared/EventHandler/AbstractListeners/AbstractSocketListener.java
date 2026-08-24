package org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.Collection;


@RequiredArgsConstructor
public abstract class AbstractSocketListener<T> {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final String defaultDestination;


    protected void sendMessage(String destination, T message) {
        simpMessagingTemplate.convertAndSend(this.defaultDestination + destination, message);
    }

    protected  void sendMessage(String destination, T message, Collection<User> users) {
        users.forEach(user ->
                sendMessage(destination, message));
    }
}
