package org.tracker.ubus.ubus.Components.User.Mapper;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.User.DTOs.Responses.UserProfileResponse;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import org.tracker.ubus.ubus.Components.User.Enum.Route;

@Component
public class UserMapper {


    public UserProfileResponse toDTO(User user, BusPreference busPreference) {

        Route route = busPreference != null ? busPreference.getRoute() :
                null;

        String stringPreference = "";
        if(route != null)
            stringPreference = route.getLabel();

        String studentNumber = "";
        String phoneNumber = user.getPhoneNumber() == null ? "No Phone Number" : user.getPhoneNumber();

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
