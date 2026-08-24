package org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response;


import lombok.Builder;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

import java.util.List;
import java.util.Map;

@Builder
public record BusPreferenceResponse(List<BusPrefView> busPreferences) {

    public static BusPreferenceResponse of(List<BusPrefView> busPreferences) {
        return BusPreferenceResponse.builder().busPreferences(busPreferences)
                .build();
    }
}
