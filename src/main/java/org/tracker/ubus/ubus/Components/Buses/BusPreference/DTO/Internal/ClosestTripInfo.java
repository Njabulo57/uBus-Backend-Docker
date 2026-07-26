package org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Internal;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;

@Builder
public record ClosestTripInfo(String eta, String distance, double distanceInKM, String busName,DelayStatus delayStatus, Trip trip) {
}
