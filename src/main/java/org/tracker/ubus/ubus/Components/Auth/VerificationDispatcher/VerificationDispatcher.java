package org.tracker.ubus.ubus.Components.Auth.VerificationDispatcher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Auth.Events.OtpEmailVerificationEvent;
import org.tracker.ubus.ubus.Components.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordExistsException;
import org.tracker.ubus.ubus.Components.OneTimePassword.Service.Impl.OneTimePasswordService;
import org.tracker.ubus.ubus.Components.TokenGenerators.EmailVerificationToken.EmailVerificationTokenService.EmailVerificationTokenService;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import org.tracker.ubus.ubus.Components.User.Enum.UserRole;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class VerificationDispatcher {

    private final MultiEvenPublisher publisher;
    private final OneTimePasswordService oneTimePasswordService;
    private final EmailVerificationTokenService emailVerificationTokenService;

    public void dispatch(User user) throws OneTimePasswordExistsException {
        if (Objects.requireNonNull(user.getRole()) == UserRole.STUDENT) {
            sendOtp(user);
        }
    }



    private void sendOtp(User user) {
        var internalCarrier = this.oneTimePasswordService.generateOTP(user.getId());
        publisher.publish(() -> new OtpEmailVerificationEvent(this, user, internalCarrier));
    }

}
