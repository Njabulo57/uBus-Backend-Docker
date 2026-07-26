package org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response;

import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

public record BusPrefView(Destination from, Destination to) {
}
