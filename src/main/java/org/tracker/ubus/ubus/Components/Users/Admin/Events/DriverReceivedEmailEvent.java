package org.tracker.ubus.ubus.Components.Users.Admin.Events;


import lombok.Getter;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractEvents.EmailEvent;
import org.tracker.ubus.ubus.Components.Shared.Mail.Templates.Admin.Templates;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

@Getter
public class DriverReceivedEmailEvent extends EmailEvent {

    private final User user;

    public DriverReceivedEmailEvent(Object source, User user) {
        super(source);
        this.user = user;
        this.setHeader("Registration Received");
        this.setToEmail(user.getEmail());
        this.setBody(constructHtmlBody());
    }

    @Override
    protected String constructHtmlBody() {
        var firstName = user.getFirstname();
        var lastName = user.getLastname();
        return Templates.buildDriverRegistrationSuccessHtml(firstName,
                lastName);
    }
}
