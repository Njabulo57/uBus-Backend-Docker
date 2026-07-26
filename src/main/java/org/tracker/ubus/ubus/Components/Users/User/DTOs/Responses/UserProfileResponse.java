package org.tracker.ubus.ubus.Components.Users.User.DTOs.Responses;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@NoArgsConstructor
public class UserProfileResponse {

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

}
