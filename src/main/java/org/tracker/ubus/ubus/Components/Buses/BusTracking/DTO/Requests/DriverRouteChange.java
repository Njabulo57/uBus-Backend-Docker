package org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests;

import java.util.UUID;

public record DriverRouteChange(double latitude, double longitude, UUID tripId) {
}
