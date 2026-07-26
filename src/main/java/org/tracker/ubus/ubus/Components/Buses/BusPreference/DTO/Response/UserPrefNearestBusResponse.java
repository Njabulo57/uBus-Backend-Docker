package org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

@Builder
public record UserPrefNearestBusResponse(int stops, Destination from, Destination to,
                                         boolean isAtUserStop, int totalStopsForJourney,
                                         ClosetTripInfoResponse closestTripResponse) {
}
