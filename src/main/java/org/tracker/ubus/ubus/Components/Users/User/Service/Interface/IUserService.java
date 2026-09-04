package org.tracker.ubus.ubus.Components.Users.User.Service.Interface;


import org.tracker.ubus.ubus.Components.Users.User.DTOs.Requests.EditUserDTO;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Responses.UserProfileResponse;

import java.util.UUID;

public interface IUserService {


    UserProfileResponse viewProfile();

    UserProfileResponse editProfile(EditUserDTO editUserDTO);

    boolean forgotPassword(String email);

    boolean changePassword(String email, String newPassword, String otp);

    void deactivateAccount(String password);

    void assignNfcCode(String nfcCode);

    void adminAssignNfc(String nfcCode, UUID userId);

    String getNfcCode();

    boolean isNfcCodeAssigned();

    boolean hasTrip();
}
