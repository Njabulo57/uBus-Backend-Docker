package org.tracker.ubus.ubus.Components.Auth.Events;


import lombok.Getter;
import org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents.EmailEvent;
import org.tracker.ubus.ubus.Components.User.Entity.User;


@Getter
public class OtpEmailVerificationEvent extends EmailEvent {

    private final String otpCode;
    private final User user;

    public OtpEmailVerificationEvent(Object source, String otpCode, User user) {
        super(source);
        this.otpCode = otpCode;
        this.user = user;
        this.setBody(constructHtmlBody()); //construct the body of the email
        this.setHeader(""); //set the header of the email
    }

    @Override
    protected String constructHtmlBody() {
        return ""; //we will call a string represenation of thwe email here
    }
}
