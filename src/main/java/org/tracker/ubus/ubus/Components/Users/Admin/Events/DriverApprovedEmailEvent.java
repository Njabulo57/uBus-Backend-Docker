package org.tracker.ubus.ubus.Components.Users.Admin.Events;


import lombok.Getter;
import org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents.EmailEvent;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;


@Getter
public final class DriverApprovedEmailEvent extends EmailEvent {

    private final User user;

    public DriverApprovedEmailEvent(Object source, User user) {
        super(source);
        this.user = user;
    }

    @Override
    protected String constructHtmlBody() {
        return "";
    }
}
