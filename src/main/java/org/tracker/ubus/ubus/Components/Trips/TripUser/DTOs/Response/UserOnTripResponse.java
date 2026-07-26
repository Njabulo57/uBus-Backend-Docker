package org.tracker.ubus.ubus.Components.Trips.TripUser.DTOs.Response;

import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

public record UserOnTripResponse(Destination from, Destination to, String arrivalTime, String departureTime) {
}
