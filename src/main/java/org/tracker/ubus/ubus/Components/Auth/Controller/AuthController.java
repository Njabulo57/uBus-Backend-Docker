package org.tracker.ubus.ubus.Components.Auth.Controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.EmailOtpRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.LoginRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.RegisterRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.EmailAuthenticationResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.LoginSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.RegisterSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.Service.Interface.IAuthService;

/**
 * Controller for handling authentication-related endpoints.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {


    private final IAuthService authService;


    /**
     * Authenticates a user with their credentials.
     * @param loginRequest the login request containing the user's credentials.
     * @return a successful login response containing the user's JWT token.
     * {auth/login}
     */
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginSuccessfulResponse login(@RequestBody @Valid final LoginRequest loginRequest)  {
        return authService.login(loginRequest);
    }


    /**
     * Initiates the email authentication process.
     * @param emailOtpRequest the email authentication request containing the user's email.
     * @return a successful email authentication response.
     * {auth/email-authentication}
     */
    @PostMapping("/email-authentication")
    public EmailAuthenticationResponse requestEmailAuthentication(@RequestBody @Valid final EmailOtpRequest emailOtpRequest) {
        return authService.requestEmailVerification(emailOtpRequest);
    }


    /**
     * Registers a new user in the system.
     *
     * @param registerRequest the registration request containing the user's details such as
     *                        first name, last name, password, role, email, and optional phone number.
     * @return a successful registration response containing details such as an OTP message,
     *         the assigned role, and the timestamp of the creation.
     * {auth/register}
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterSuccessfulResponse register(@RequestBody @Valid final RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }


    /**
     * Verifies if the provided email address is valid and can be used within the system.
     *
     * @param email the email address to be verified.
     * @return true if the email is valid and accepted, false otherwise.
     * {auth/verify-email}
     * HTTP Method: POST
     *
     */
    @PostMapping("/verify-email")
    public boolean verifyEmail(@RequestParam String email) {
        return this.authService.verifyEmail(email);
    }


    /**
     * Logs the user out of the current session.
     * After invoking this method, the user's authentication credentials are invalidated,
     * effectively terminating their access to secure endpoints until they log in again.
     * Endpoint: {auth/logout}
     * HTTP Method: POST
     * Response Status: 204 NO CONTENT
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        authService.logout();
    }

}