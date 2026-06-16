package org.tracker.ubus.ubus.Components.Users.User.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Requests.EditUserDTO;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Responses.UserProfileResponse;
import org.tracker.ubus.ubus.Components.Users.User.Service.Interface.IUserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
class UserController {

    private final IUserService userService;


    @GetMapping("/view-profile")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponse viewProfile() {
        return userService.viewProfile();
    }

    @PutMapping("/edit-profile")
    public UserProfileResponse editProfile(@RequestBody EditUserDTO editUserDTO) {
        return userService.editProfile(editUserDTO);
    }
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.CONTINUE)
    public void forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
    }

    @PutMapping("/valid-forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public void validForgotPassword(@RequestParam String email,
                                    @RequestParam String otp,
                                    @RequestParam String newPassword) {
        userService.changePassword(email, otp, newPassword);
    }

}
