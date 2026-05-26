package org.tracker.ubus.ubus.Components.BusTracking.DTO.Responses;

import lombok.Builder;

import java.time.LocalTime;
import java.util.UUID;


@Builder
public record DriverCurrentLocationResponse(UUID busId, String route,
                                            LocalTime eta,
                                            DriverCurrentLocationDelay delay) {

}
