package org.tracker.ubus.ubus.Components.Notification.Events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.tracker.ubus.ubus.Components.Notification.Enum.NotificationType;
import java.util.Queue;
import java.util.UUID;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class Notification {

    private String message;
    private NotificationType notificationType;
    private Queue<UUID> userToNotify;


    protected abstract void constructMessage();


}
