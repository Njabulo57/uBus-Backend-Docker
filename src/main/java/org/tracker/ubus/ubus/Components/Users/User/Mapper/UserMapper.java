package org.tracker.ubus.ubus.Components.Users.User.Mapper;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Responses.StudentStaffUserResponse;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Responses.UserProfileResponse;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMapper {


    public UserProfileResponse toDTO(User user, List<BusPreference> busPreferences, int completedTrips) {

        List<String> stringPreferences = busPreferences.stream()
                .map(busPreference -> busPreference.getRoute().getLabel())
                .toList();


        if(user.getRole() == UserRole.STUDENT || user.getRole() == UserRole.STAFF)
           return StudentStaffUserResponse.builder()
                   .totalTrips(completedTrips)
                   .busPreferences(stringPreferences)
                   .firstName(user.getFirstname())
                   .lastName(user.getLastname())
                   .email(user.getEmail())
                   .totalRoutes(busPreferences.size())
                   .phoneNumber(user.getPhoneNumber())
                   .build();


        return  UserProfileResponse.builder()
                .firstName(user.getFirstname())
                .lastName(user.getLastname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
