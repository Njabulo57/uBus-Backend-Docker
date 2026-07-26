package org.tracker.ubus.ubus.Components.Users.User.DTOs.Responses;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class StudentStaffUserResponse extends UserProfileResponse {

    private int totalTrips;
    private int totalRoutes;
    private List<String> busPreferences;
}
