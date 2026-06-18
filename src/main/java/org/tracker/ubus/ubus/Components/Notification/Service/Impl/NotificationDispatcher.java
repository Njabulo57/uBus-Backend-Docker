package org.tracker.ubus.ubus.Components.Notification.Service.Impl;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Notification.Events.Notification;
import org.tracker.ubus.ubus.Components.Notification.Service.Interface.INotificationService;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class NotificationDispatcher implements INotificationService {

    private final SimpMessagingTemplate broker;
    private final String socketRoute = "/queue/notifications";


    /**
     * Send a notification to users
     * @param notification the notification to be sent
     */
    @Override
    public void sendNotification(Notification notification) {
        notification.getUserToNotify()
                .forEach(user ->
                        broker.convertAndSend(socketRoute,
                                notification));
    }


    @Override
    public void sendNotifications(Collection<Notification> notifications) {
        notifications.forEach(this::sendNotification);
    }

}
