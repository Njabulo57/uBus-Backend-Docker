package org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;

@Builder
public record ClosetTripInfoResponse(String eta, String distance, double distanceInKM, String busName,
                                     DelayStatus delayStatus) {
}
