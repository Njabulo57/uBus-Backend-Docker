package org.tracker.ubus.ubus.Components.Users.User.DTOs.Requests;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EditUserDTO {
    private String firstname;

    private String lastname;

    private String email;

    private String newPassword;
    private String oldPassword;

    private String phoneNumber;
    private String otp;
}
