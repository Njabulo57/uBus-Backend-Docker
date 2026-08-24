package org.tracker.ubus.ubus.Components.Buses.BusTracking.BusPromixityChecker.DTO;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;

@Builder
public record BusJourneyTripProgression(
        String message, int progression, Trip trip) {

    public static BusJourneyTripProgression of(Trip trip, int progression, String message) {
        return BusJourneyTripProgression.builder()
                .trip(trip)
                .progression(progression)
                .message(message)
                .build();
    }
}
