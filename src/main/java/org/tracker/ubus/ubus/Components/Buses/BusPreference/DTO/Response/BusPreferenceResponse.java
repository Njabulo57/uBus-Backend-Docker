package org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response;


import lombok.Builder;

import java.util.List;

@Builder
public record BusPreferenceResponse(List<String> busPreferences) {
}
