package org.tracker.ubus.ubus.Components.Trips.TripLate.DTO.Request;

import java.util.UUID;

public record TripLateRequest(String reason, String description, UUID tripId) {
}
