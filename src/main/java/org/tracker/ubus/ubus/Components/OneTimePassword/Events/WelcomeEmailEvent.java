package org.tracker.ubus.ubus.Components.OneTimePassword.Events;

import org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents.EmailEvent;
import org.tracker.ubus.ubus.Components.Shared.Mail.Templates.Students.Templates;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;


public final class WelcomeEmailEvent extends EmailEvent {

    private final User user;

    public WelcomeEmailEvent(Object source, User user) {
        super(source);
        this.user = user;
        setHeader("Welcome to Ubus");
        setToEmail(user.getEmail());
        setBody(constructHtmlBody());
    }

    @Override
    protected String constructHtmlBody() {
        var firstName = user.getFirstname();
        var lastName = user.getLastname();
        return Templates.buildWelcomeEmailHtml(firstName, lastName);
    }
}
