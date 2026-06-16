package org.tracker.ubus.ubus.Components.Notification.Service.Interface;

import org.tracker.ubus.ubus.Components.Notification.Events.Notification;

import java.util.Collection;


public interface INotificationService {

    void sendNotification(Notification notification);

    void sendNotifications(Collection<Notification> notifications);
}
