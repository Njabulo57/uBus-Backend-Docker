package org.tracker.ubus.ubus.Components.User.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.tracker.ubus.ubus.Components.User.DTOs.Responses.UserProfileResponse;
import org.tracker.ubus.ubus.Components.User.Service.Interface.IUserService;

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

}
