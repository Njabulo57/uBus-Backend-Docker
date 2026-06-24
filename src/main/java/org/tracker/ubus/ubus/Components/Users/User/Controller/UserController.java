package org.tracker.ubus.ubus.Components.Users.User.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Requests.EditUserDTO;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Responses.UserProfileResponse;
import org.tracker.ubus.ubus.Components.Users.User.Service.Interface.IUserService;

/**
 * REST Controller for managing user operations.
 *
 * Handles endpoints for user profile viewing, editing, and password management.
 * All endpoints are mapped to the /users base path.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
class UserController {

    private final IUserService userService;

    /**
     * Retrieves the current user's profile information.
     *
     * HTTP Method: GET
     * Endpoint: /users/view-profile
     *
     * @return UserProfileResponse containing:
     *         - firstName: The user's first name
     *         - lastName: The user's last name
     *         - email: The user's email address
     *         - phoneNumber: The user's phone number
     *         - studentNumber: The user's student number
     *         - busPreferences: List of bus route preferences
     */
    @GetMapping("/view-profile")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponse viewProfile() {
        return userService.viewProfile();
    }

    /**
     * Updates the current user's profile information.
     *
     * HTTP Method: PUT
     * Endpoint: /users/edit-profile
     *
     * @param editUserDTO the user profile data to be updated containing:
     *        - firstname: The user's first name
     *        - lastname: The user's last name
     *        - email: The user's email address
     *        - newPassword: The new password (optional if not changing password)
     *        - oldPassword: The current password (required for authentication)
     *        - phoneNumber: The user's phone number
     * @return UserProfileResponse containing the updated user's profile details
     */
    @PutMapping("/edit-profile")
    public UserProfileResponse editProfile(@RequestBody EditUserDTO editUserDTO) {
        return userService.editProfile(editUserDTO);
    }

    /**
     * Initiates a forgot password request by sending an OTP to the user's email.
     *
     * HTTP Method: POST
     * Endpoint: /users/forgot-password
     *
     * @param editUserDTO the user data containing:
     *        - email: The user's email address
     */
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.CONTINUE)
    public void forgotPassword(@RequestBody EditUserDTO editUserDTO) {
        userService.forgotPassword(editUserDTO.getEmail());
    }

    /**
     * Validates the OTP and changes the user's password.
     *
     * HTTP Method: PUT
     * Endpoint: /users/valid-forgot-password
     *
     * @param editUserDTO the user data containing:
     *        - email: The user's email address
     *        - otp: The one-time password sent to the user's email
     *        - newPassword: The new password to be set
     */
    @PutMapping("/valid-forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public void validForgotPassword(@RequestBody EditUserDTO editUserDTO) {
        userService.changePassword(editUserDTO.getEmail(), editUserDTO.getOtp(), editUserDTO.getNewPassword());
    }

    /**
     *
     * Validates the password and deactivates account
     *
     *       HTTP Method: PUT
     *       Endpoint: /users/deactivate-account
     * @param password the password of the user
     */
    @PutMapping("/deactivate-account")
    @ResponseStatus(HttpStatus.OK)
    public void deactivateAccount(@RequestParam String password) {
        userService.deactivateAccount(password);
    }

}