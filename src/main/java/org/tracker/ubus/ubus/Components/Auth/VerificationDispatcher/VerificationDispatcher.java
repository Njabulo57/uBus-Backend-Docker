package org.tracker.ubus.ubus.Components.Auth.VerificationDispatcher;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Auth.Events.OtpEmailVerificationEvent;
import org.tracker.ubus.ubus.Components.Auth.Service.Interface.AuthTokenGenerationService;
import org.tracker.ubus.ubus.Components.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordExistsException;
import org.tracker.ubus.ubus.Components.OneTimePassword.Service.Interface.IOneTimePasswordService;
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

    private final MultiEvenPublisher publisher;
    private final AuthTokenGenerationService oneTimePasswordService;


    public VerificationDispatcher(MultiEvenPublisher publisher,
                                  IOneTimePasswordService oneTimePasswordService) {

        this.publisher = publisher;
        this.oneTimePasswordService = oneTimePasswordService;
    }


    /**
     * Dispatches a one-time password (OTP) for user registration based on the user's role.
     * This method generates and sends an OTP to the user if their role is either STAFF or STUDENT.
     * If an OTP already exists for the user, the operation will throw an exception.
     *
     * @param user the user for whom the registration OTP is being dispatched.
     *             The user must have a valid role of either STAFF or STUDENT, as determined by the system.
     * @throws OneTimePasswordExistsException if an existing OTP for the user is still valid and has not yet expired.
     */
    public void dispatchRegistrationOTP(User user) throws OneTimePasswordExistsException {
        switch (user.getRole()) {
            case STAFF, STUDENT: sendOtp(user);
        }
    }


    private void sendOtp(User user) {
        var internalCarrier = this.oneTimePasswordService.generateAuthToken(user.getId());
        publisher.publish(() -> new OtpEmailVerificationEvent(this, user, internalCarrier));
    }

}
