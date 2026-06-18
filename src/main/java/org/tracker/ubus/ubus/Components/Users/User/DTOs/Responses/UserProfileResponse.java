package org.tracker.ubus.ubus.Components.Users.User.DTOs.Responses;


import lombok.Builder;

import java.util.List;

@Builder
public record UserProfileResponse(String firstName, String lastName,
                                  String email, String phoneNumber,
                                  String studentNumber, List<String> busPreferences) {
}
