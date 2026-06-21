package org.tracker.ubus.ubus.Components.Users.PotentialAdmin.Events;

import org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents.EmailEvent;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;

public final class PendingAdminAdditionEvent extends EmailEvent {

    private final OtpInternalCarrier otpInternalCarrier;

    public PendingAdminAdditionEvent(Object source, String subject, OtpInternalCarrier otpInternalCarrier) {
        super(source);

        setHeader("Admin Approval Pending");
        setToEmail(subject);
        this.otpInternalCarrier = otpInternalCarrier;
    }

    @Override
    protected String constructHtmlBody() {
        var otp = otpInternalCarrier.opt();
        var expiry = otpInternalCarrier.expiry();
        return "";
    }
}
