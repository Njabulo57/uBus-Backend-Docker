package org.tracker.ubus.ubus.Components.BusTracking.DTO.Responses;

import java.util.UUID;

public record DriverCurrentLocationResponse(UUID busId, double latitude,
                                           double longitude) {
}
