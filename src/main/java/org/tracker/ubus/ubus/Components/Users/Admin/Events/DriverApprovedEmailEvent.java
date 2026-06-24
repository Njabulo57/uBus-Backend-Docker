package org.tracker.ubus.ubus.Components.Users.Admin.Events;


import lombok.Getter;
import org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents.EmailEvent;
import org.tracker.ubus.ubus.Components.Shared.Mail.Templates.Admin.Templates;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;


@Getter
public final class DriverApprovedEmailEvent extends EmailEvent {

    private final User user;

    public DriverApprovedEmailEvent(Object source, User user) {
        super(source);
        this.user = user;

        setHeader("Driver Approved");
        setToEmail(user.getEmail());
        setBody(constructHtmlBody());
    }

    @Override
    protected String constructHtmlBody() {
        var firstName = user.getFirstname();
        var lastName = user.getLastname();
        return Templates.buildDriverAssignedHtml(firstName, lastName);
    }
}
