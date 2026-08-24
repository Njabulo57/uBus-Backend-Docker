package org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Internal.ClosestTripInfo;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;


@Builder
public record UserPreferenceNearestBus(int stops, Destination from, Destination to,
                                       User user,
                                       boolean isAtUserStop, int totalStopsForJourney,
                                       ClosestTripInfo nearestTrip, int progress) {
}
