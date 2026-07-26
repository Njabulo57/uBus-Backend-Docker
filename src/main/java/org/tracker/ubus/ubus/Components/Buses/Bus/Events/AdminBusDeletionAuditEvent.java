package org.tracker.ubus.ubus.Components.Buses.Bus.Events;

import lombok.Getter;
import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractEvents.AuditEvent;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

@Getter
public final class AdminBusDeletionAuditEvent extends AuditEvent implements AdminCRAUDAuditEvent  {

    private final User admin;


    public AdminBusDeletionAuditEvent(Object source, User admin, Bus bus) {
        super(source,AuditType.ADMIN_BUS_DELETION);

        this.admin = admin;
        var adminName = formatAdminName(admin);
        var message = getAuditType().format(bus.getName(), adminName, getFormattedDate());
        setMessage(message);

    }
}
