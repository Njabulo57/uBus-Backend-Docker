package org.tracker.ubus.ubus.Components.Admin.Events;

import lombok.Getter;

import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;
import org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents.AuditEvent;
import org.tracker.ubus.ubus.Components.User.Entity.User;

@Getter
public final class DriverApprovedAuditEvent extends AuditEvent {



    public DriverApprovedAuditEvent(Object source,
                                    User createdBy, User createdOn) {
        super(source, AuditType.ADMIN_DRIVER_APPROVAL, createdBy, createdOn);
    }
}
