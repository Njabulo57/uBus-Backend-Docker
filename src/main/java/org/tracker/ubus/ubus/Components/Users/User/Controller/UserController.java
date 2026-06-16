package org.tracker.ubus.ubus.Components.Users.User.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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

}
