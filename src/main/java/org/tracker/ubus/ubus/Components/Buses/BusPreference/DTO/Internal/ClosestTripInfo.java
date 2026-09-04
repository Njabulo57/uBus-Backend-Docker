package org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Internal;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosestTripInfo {

    private String eta;

    private String distance;

    private double distanceInKM;

    private String busName;

    private DelayStatus delayStatus;

    private Trip trip;

    private int progress;

    private String registrationPlate;
}
