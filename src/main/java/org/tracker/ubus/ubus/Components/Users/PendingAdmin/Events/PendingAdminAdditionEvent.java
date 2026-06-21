package org.tracker.ubus.ubus.Components.Users.PendingAdmin.Events;

import org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents.EmailEvent;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;

public final class PendingAdminAdditionEvent extends EmailEvent {

    private final OtpInternalCarrier otpInternalCarrier;

    public PendingAdminAdditionEvent(Object source, String subject, OtpInternalCarrier otpInternalCarrier) {
        super(source);

        this.otpInternalCarrier = otpInternalCarrier;
        setHeader("Admin Approval Pending");
        setToEmail(subject);
        setBody(constructHtmlBody());

    }

    @Override
    protected String constructHtmlBody() {
        var otp = otpInternalCarrier.opt();
        var expiry = otpInternalCarrier.expiry();
        return "Hello. Your OTP is " + otp + " and it will expire in " + expiry + " minutes.";
    }
}
