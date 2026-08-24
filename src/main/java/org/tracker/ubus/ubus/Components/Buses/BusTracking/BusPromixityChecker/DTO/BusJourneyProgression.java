package org.tracker.ubus.ubus.Components.Buses.BusTracking.BusPromixityChecker.DTO;


import lombok.Builder;

@Builder
public record BusJourneyProgression(String message, int progression) {
}
