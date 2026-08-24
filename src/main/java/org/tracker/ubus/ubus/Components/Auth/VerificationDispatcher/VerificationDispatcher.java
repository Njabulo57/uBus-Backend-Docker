package org.tracker.ubus.ubus.Components.Auth.VerificationDispatcher;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Auth.Events.OtpEmailVerificationEvent;
import org.tracker.ubus.ubus.Components.Auth.Service.Interface.AuthTokenGenerationService;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.Publisher.MultiEventPublisher;
import org.tracker.ubus.ubus.Components.OneTimePassword.Service.Interface.IOneTimePasswordService;
import org.tracker.ubus.ubus.Components.Users.Admin.Events.DriverReceivedEmailEvent;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;



/**
 * The VerificationDispatcher is responsible for dispatching verification processes,
 * such as generating and sending One-Time Passwords (OTPs) for user registration
 * and forgot password functionalities. This class utilizes different services and
 * publishers to generate authentication tokens and publish verification events to users.
 * Dependencies include:
 * - {@code MultiEvenPublisher}: Publishes application events.
 * - {@code AuthTokenGenerationService}: Responsible for generating authentication tokens.
 * - {@code IOneTimePasswordService and IForgotPasswordService}: Specialized services for
 *   handling OTP-related operations for registration and forgot password processes.
 */
@Component
public class VerificationDispatcher {

    private final MultiEventPublisher publisher;
    private final AuthTokenGenerationService oneTimePasswordService;


    public VerificationDispatcher(MultiEventPublisher publisher,
                                  IOneTimePasswordService oneTimePasswordService) {

        this.publisher = publisher;
        this.oneTimePasswordService = oneTimePasswordService;
    }


    public void dispatchVerification(User user) {
        var role = user.getRole();
        switch (role) {
            case STAFF, STUDENT: sendOtp(user);
                break;

            case DRIVER: sendReceivedEmail(user);
                break;
        }
    }


    private void sendOtp(User user) {
        var internalCarrier = this.oneTimePasswordService.generateAuthToken(user.getId());
        publisher.publish(() -> new OtpEmailVerificationEvent(this, user, internalCarrier));
    }

    private void sendReceivedEmail(User user) {
        publisher.publish(() -> new DriverReceivedEmailEvent(this, user));
    }

}
