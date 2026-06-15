package org.tracker.ubus.ubus.Components.Auth.Service.Interface;


import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.ChangePasswordRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.EmailOtpRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.LoginRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.RegisterRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.EmailAuthenticationResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.LoginSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.RegisterSuccessfulResponse;

public interface IAuthService {

    /**
     * Authenticates a user with their credentials and returns a successful login response.
     *
     * @param loginRequest the login request containing the user's email and password.
     * @return a {@code LoginSuccessfulResponse} containing the generated JWT token, the user's role,
     *         and the timestamp of the login.
     */
    LoginSuccessfulResponse login(LoginRequest loginRequest);

    /**
     * Registers a new user in the system with the provided details.
     *
     * @param registerRequest the registration request containing the user's first name, last name,
     *                        password, role, email, and optional phone number.
     * @return a {@code RegisterSuccessfulResponse} containing an OTP message,
     *         the assigned role, and the timestamp of the account creation.
     */
    RegisterSuccessfulResponse register(RegisterRequest registerRequest);

    /**
     * Verifies if a given email address is valid and can be used within the system.
     *
     * @param email the email address to be verified.
     * @return true if the email is valid and accepted, false otherwise.
     */
    boolean verifyEmail(String email);

    /**
     * Initiates the process to send an OTP for email verification to the specified email address.
     *
     * @param emailOtpRequest the request containing the email address that will receive the OTP.
     * @return an {@code EmailAuthenticationResponse} containing a message and the timestamp of the request.
     */
    EmailAuthenticationResponse requestEmailVerification(EmailOtpRequest emailOtpRequest);

}
