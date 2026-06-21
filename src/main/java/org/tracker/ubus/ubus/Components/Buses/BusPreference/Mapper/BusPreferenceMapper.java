package org.tracker.ubus.ubus.Components.Buses.BusPreference.Mapper;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Responses.UserProfileResponse;

import java.util.ArrayList;
import java.util.List;

@Component
public class BusPreferenceMapper {

    public BusPreferenceResponse toResponse(List<BusPreference> busPreferences) {
        List<String> stringPreferences = new ArrayList<>();
        if (busPreferences != null)
            for (BusPreference busPreference : busPreferences) {
                stringPreferences.add(busPreference.getRoute().getLabel());
            }


        return BusPreferenceResponse.builder()

                .busPreferences(stringPreferences)
                .build();
    }
}

