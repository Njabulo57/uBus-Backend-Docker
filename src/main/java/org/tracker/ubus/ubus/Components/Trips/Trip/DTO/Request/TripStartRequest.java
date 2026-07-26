package org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request;

import java.util.UUID;

public record TripStartRequest(double latitude, double longitude,
                               UUID tripId) {
}
