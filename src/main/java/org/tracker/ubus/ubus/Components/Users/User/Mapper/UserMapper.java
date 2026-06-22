package org.tracker.ubus.ubus.Components.Users.User.Mapper;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Responses.UserProfileResponse;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMapper {


    public UserProfileResponse toDTO(User user, List<BusPreference> busPreferences) {

        List<String> stringPreferences = new ArrayList<>();
        for(BusPreference busPreference : busPreferences)
            stringPreferences.add(busPreference.getRoute().getLabel());



        String studentNumber = user.getStudentNumber();
        String phoneNumber = user.getPhoneNumber();

        return UserProfileResponse.builder()
                .firstName(user.getFirstname())
                .lastName(user.getLastname())
                .email(user.getEmail())
                .phoneNumber(phoneNumber)
                .studentNumber(studentNumber)
                .busPreferences(stringPreferences)
                .build();
    }
}
