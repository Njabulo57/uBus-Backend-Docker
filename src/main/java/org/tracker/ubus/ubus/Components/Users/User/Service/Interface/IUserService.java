package org.tracker.ubus.ubus.Components.Users.User.Service.Interface;


import org.tracker.ubus.ubus.Components.Users.User.DTOs.Requests.EditUserDTO;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Responses.UserProfileResponse;

public interface IUserService {


    UserProfileResponse viewProfile();

    UserProfileResponse editProfile(EditUserDTO editUserDTO);

    boolean forgotPassword(String email);

    boolean changePassword(String email, String newPassword, String otp);

    void deactivateAccount(String password);
}
