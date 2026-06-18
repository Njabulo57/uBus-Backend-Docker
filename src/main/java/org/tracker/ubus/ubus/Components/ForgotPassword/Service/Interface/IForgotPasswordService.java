package org.tracker.ubus.ubus.Components.ForgotPassword.Service.Interface;

import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.ChangePasswordRequest;
import org.tracker.ubus.ubus.Components.Auth.Service.Interface.AuthTokenGenerationService;
import org.tracker.ubus.ubus.Components.ForgotPassword.DTO.Request.ResetPasswordRequest;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;

import java.util.UUID;

/**
 * Interface for the ForgotPasswordService.
 * Provides methods for generating and resetting passwords.
 */
public interface IForgotPasswordService extends AuthTokenGenerationService {


    /**
     * Resets the password for a user using the provided OTP.
     * @param changePasswordRequest the request containing the new password and OTP.
     */
    void resetPassword(ResetPasswordRequest changePasswordRequest);


}
