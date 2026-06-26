package org.tracker.ubus.ubus.Components.Users.Admin.Events;

import lombok.Getter;
import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;
import org.tracker.ubus.ubus.Components.Buses.Bus.Events.AdminCRAUDAuditEvent;
import org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents.AuditEvent;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.time.LocalDateTime;


@Getter
public final class AdminDriverAssignmentAuditEvent extends AuditEvent implements AdminCRAUDAuditEvent {

    private final User createdBy;
    private final User createdOn;

    public AdminDriverAssignmentAuditEvent(Object source, User createdBy,
                                           User createdOn) {
        super(source,AuditType.ADMIN_DRIVER_APPROVAL);
        this.createdBy = createdBy;
        this.createdOn = createdOn;

        final var formattedMessage = formatMessage(getAuditType(), createdBy, createdOn, LocalDateTime.now());
        setMessage(formattedMessage);
    }


    @Override
    public String formatMessage(AuditType auditType, User createdBy, User createdOn, LocalDateTime timeStamp) {
        String firstName = createdOn.getFirstname();
        String lastName = createdOn.getLastname();

        String adminsName = formatAdminName(createdBy);
        String[] args = new String[]{ firstName, lastName , adminsName, getFormattedDate()};
        return auditType.format(args);
    }
}
