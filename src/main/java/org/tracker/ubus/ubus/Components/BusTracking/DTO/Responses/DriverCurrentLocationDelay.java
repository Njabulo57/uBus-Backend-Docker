package org.tracker.ubus.ubus.Components.BusTracking.DTO.Responses;

import lombok.Builder;

import java.time.LocalTime;

@Builder
public record DriverCurrentLocationDelay(boolean isDelayed, LocalTime delayTime) {
}
