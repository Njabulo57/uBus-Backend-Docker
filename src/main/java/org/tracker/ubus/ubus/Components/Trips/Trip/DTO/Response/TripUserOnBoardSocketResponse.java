package org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

@Builder
public record TripUserOnBoardSocketResponse(Destination from, Destination to, String arrivalTime,
                                            String departureTime, String route) {
}
