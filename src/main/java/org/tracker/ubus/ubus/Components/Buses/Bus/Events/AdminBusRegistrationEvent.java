package org.tracker.ubus.ubus.Components.Buses.Bus.Events;

import lombok.Getter;

import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents.AuditEvent;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

@Getter
public final class AdminBusRegistrationEvent extends AuditEvent implements AdminCRAUDAuditEvent {

    private final User admin;


    public AdminBusRegistrationEvent(Object source, Bus bus, User admin) {
        super(source,AuditType.ADMIN_BUS_REGISTRATION);
        this.admin = admin;
        var adminName = formatAdminName(admin);
        var message = getAuditType().format(bus.getName(), adminName, getFormattedDate());
        setMessage(message);
    }

}
