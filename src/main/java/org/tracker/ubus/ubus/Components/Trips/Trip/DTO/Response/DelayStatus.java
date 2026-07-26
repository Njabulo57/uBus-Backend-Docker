package org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response;

import java.time.LocalTime;

public record DelayStatus(boolean isDelayed, long delayInMinutes, LocalTime eta, String arrivalTime) {
}
