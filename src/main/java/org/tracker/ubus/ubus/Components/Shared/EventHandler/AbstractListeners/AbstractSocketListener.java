package org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.Collection;


@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSocketListener<T> {


    private final SimpMessagingTemplate simpMessagingTemplate;
    private final String defaultDestination;


    /**
     * Sends a message to a specific destination by appending the provided destination
     * to the default destination prefix and delivering the message payload.
     *
     * @param destination the relative destination to which the message will be sent
     * @param message     the message payload of type T to be delivered
     */
    protected void sendMessage(String destination, T message) {
        simpMessagingTemplate.convertAndSend(this.defaultDestination + destination, message);
    }

    /**
     * Sends the specified message to a collection of users by leveraging their email addresses.
     * Each user's email is treated as the destination for the message.
     *
     * @param users   a collection of User objects to whom the message will be sent
     * @param message the message payload of type T to be delivered to users
     */
    protected void sendMessage(Collection<User> users, T message) {
        users.forEach(user -> {
                sendMessage(user.getEmail(), message);
                log.info("Message sent to user: {}", user.getEmail());
        });
    }

    /**
     * Sends a message to a specific destination for each user in the provided collection.
     *
     * @param destination the relative destination to which the message will be sent
     * @param message     the message payload of type T to be delivered
     * @param users       a collection of User objects who will receive the message
     */
    protected void sendMessage(String destination, T message, Collection<User> users) {
        users.forEach(user ->
                sendMessage(destination, message));
    }


}
