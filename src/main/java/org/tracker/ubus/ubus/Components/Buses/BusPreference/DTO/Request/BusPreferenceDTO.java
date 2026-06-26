package org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BusPreferenceDTO {
    String oldRoute;
    String newRoute;
}
