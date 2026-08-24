package org.tracker.ubus.ubus.Components.Buses.Bus.Events;

import lombok.Getter;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractEvents.EmailEvent;
import org.tracker.ubus.ubus.Components.Shared.Mail.Templates.Admin.Templates;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.List;


@Getter
public class AdminBusAssignmentRemovalEvent extends EmailEvent {


    private final Bus bus;
    private final List<User> driversAssigned;

    public AdminBusAssignmentRemovalEvent(Object source, Bus bus, List<User> driversAssigned) {
        super(source);
        setHeader("Bus Status Update");
        this.bus = bus;
        this.driversAssigned = driversAssigned;
    }


    @Override
    protected String constructHtmlBody() {
        return "";
    }


    public String getBodyWithDriver(User driver) {

        var status = bus.getOperationalStatus().name();
        var driverEmail = Templates.buildBusStatusHtml(driver.getFirstname(), driver.getLastname(), status);
        return driverEmail;
    }
}
