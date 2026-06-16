package org.tracker.ubus.ubus.Components.Notification.Events;


import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.util.Queue;
import java.util.UUID;


@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public final class BusDepartureNotification extends Notification {

    private String fromCampus;
    private String toCampus;

    public BusDepartureNotification(Queue<UUID> userToNotify, Route route) {

        this.fromCampus = route.getFromCampus().getLabel();
        this.toCampus = route.getToCampus().getLabel();

        constructMessage();
        this.setUserToNotify(userToNotify);

    }


    @Override
    protected void constructMessage() {
        String message = String.format("Bus is departing from %s. Headed to %s", fromCampus, toCampus);
        setMessage(message);
    }
}
