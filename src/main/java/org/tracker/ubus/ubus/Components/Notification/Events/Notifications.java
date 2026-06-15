package org.tracker.ubus.ubus.Components.Notification.Events;

import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;
import java.util.Queue;
import java.util.UUID;

public abstract class Notifications {


    public static Notification busDepartureNotification(Route route, Queue<UUID> userToNotify) {
        return BusDepartureNotification.builder()
                .fromCampus(route.getFromCampus().getLabel())
                .toCampus(route.getToCampus().getLabel())
                .userToNotify(userToNotify)
                .build();
    }

    public static Notification busFullNotification(Queue<UUID> userToNotify) {
        return BusFullNotification.builder()
                .userToNotify(userToNotify)
                .build();
    }

}
