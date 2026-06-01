package org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response;

import lombok.Builder;

import java.util.UUID;


@Builder
public record ActiveTripResponse(UUID id, UUID busId,
                                 String route, String busName, String busStatus) {
}
