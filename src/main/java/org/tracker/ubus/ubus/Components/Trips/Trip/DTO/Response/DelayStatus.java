package org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response;

import lombok.Builder;

import java.time.LocalTime;

@Builder
public record DelayStatus(boolean isDelayed, long delayInMinutes, LocalTime eta, String arrivalTime) {

}
