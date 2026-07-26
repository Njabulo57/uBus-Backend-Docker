package org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

import java.util.UUID;

@Builder
public record BusPreferenceClosestTripResponse(String eta, String distance,
                                               Destination from, Destination to, String busName,DelayStatus delayStatus, int stops, UUID tripId) {
}
