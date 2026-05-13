package org.tracker.ubus.ubus.Components.Auth.VerificationDispatcher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Auth.Events.OtpEmailVerificationEvent;
import org.tracker.ubus.ubus.Components.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordExistsException;
import org.tracker.ubus.ubus.Components.OneTimePassword.Service.Impl.OneTimePasswordService;
import org.tracker.ubus.ubus.Components.TokenGenerators.EmailVerificationToken.EmailVerificationTokenService.EmailVerificationTokenService;
import org.tracker.ubus.ubus.Components.User.Entity.User;

@Component
@RequiredArgsConstructor
public class VerificationDispatcher {

    private final MultiEvenPublisher publisher;
    private final OneTimePasswordService oneTimePasswordService;
    private final EmailVerificationTokenService emailVerificationTokenService;

    public void dispatch(User user) throws OneTimePasswordExistsException {
        switch(user.getRole()) {
            case STUDENT -> sendOtp(user);
            case DRIVER, STAFF -> sendMagicLink(user);
            default -> throw new IllegalStateException(user.getRole() + " has no Supported Dispatch");
        }
    }



    private void sendOtp(User user) {
        var internalCarrier = this.oneTimePasswordService.generateOTP(user.getId());
        publisher.publish(() -> new OtpEmailVerificationEvent(this, user, internalCarrier));
    }

    private void sendMagicLink(User user) {
        // i know. ill change it when i get time
        var internalCarrier = this.oneTimePasswordService.generateOTP(user.getId());
        publisher.publish(() -> new OtpEmailVerificationEvent(this, user, internalCarrier));
    }
}
