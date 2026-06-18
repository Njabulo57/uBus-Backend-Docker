package org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request;

import java.util.UUID;


public record TripEndRequest(UUID tripId, double latitude, double longitude) {
}
