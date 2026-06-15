package org.tracker.ubus.ubus.Components.Notification.Events;

import lombok.Data;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.tracker.ubus.ubus.Components.Notification.Enum.NotificationType;

import java.util.Queue;
import java.util.UUID;


@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public final class BusFullNotification extends Notification {

    public BusFullNotification(Queue<UUID> userToNotify) {
        setUserToNotify(userToNotify);
        setNotificationType(NotificationType.BUS_FULL);
        constructMessage();
    }

    @Override
    protected void constructMessage() {
        setMessage("Bus at full capacity");
    }
}
