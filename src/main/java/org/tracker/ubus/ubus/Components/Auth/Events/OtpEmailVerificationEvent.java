package org.tracker.ubus.ubus.Components.Auth.Events;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents.EmailEvent;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;
import org.tracker.ubus.ubus.Components.Shared.Mail.Templates.OtpTemplate;
import org.tracker.ubus.ubus.Components.User.Entity.User;

import java.util.UUID;


@Getter
public class OtpEmailVerificationEvent extends EmailEvent {

    private final OtpInternalCarrier internalCarrier;
    private final String otpCode;
    private final User user;

    public OtpEmailVerificationEvent(Object source, User user,  OtpInternalCarrier internalCarrier) {
        super(source);
        this.internalCarrier = internalCarrier;
        this.otpCode = internalCarrier.opt();
        this.user = user;
        this.setBody(constructHtmlBody()); //construct the body of the email
        this.setHeader("One Time Password Verification"); //set the header of the email
    }

    @Override
    protected String constructHtmlBody() {
        String personName = user.getFirstname() + " " + user.getLastname();
        String otp = getInternalCarrier().opt();
        int expiry = getInternalCarrier().expiry();
        return OtpTemplate.otpEmailTemplate(personName,otp, expiry); //we will call a string representation of the email here
    }
}
