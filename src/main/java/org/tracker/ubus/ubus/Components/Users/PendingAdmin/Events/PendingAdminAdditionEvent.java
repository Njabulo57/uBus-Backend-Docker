package org.tracker.ubus.ubus.Components.Users.PendingAdmin.Events;

import org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents.EmailEvent;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;
import org.tracker.ubus.ubus.Components.Shared.Mail.Templates.PendingAdmin.Templates;

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
        return Templates.buildAdminInvitationEmail(otp, expiry);
    }
}
