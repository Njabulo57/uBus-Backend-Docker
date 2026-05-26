package org.tracker.ubus.ubus.Components.User.Mapper;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.User.DTOs.Responses.UserProfileResponse;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import org.tracker.ubus.ubus.Components.User.Enum.Route;
import org.tracker.ubus.ubus.Components.User.Enum.UserRole;

@Component
public class UserMapper {


    public UserProfileResponse toDTO(User user, BusPreference busPreference) {

        String stringPreference = "";
        if(busPreference != null)
            stringPreference = busPreference
                    .getRoute()
                    .getLabel();

        String studentNumber = user.getStudentNumber();
        String phoneNumber = user.getPhoneNumber();

        return UserProfileResponse.builder()
                .firstName(user.getFirstname())
                .lastName(user.getLastname())
                .email(user.getEmail())
                .phoneNumber(phoneNumber)
                .studentNumber(studentNumber)
                .busPreference(stringPreference)
                .build();
    }
}
