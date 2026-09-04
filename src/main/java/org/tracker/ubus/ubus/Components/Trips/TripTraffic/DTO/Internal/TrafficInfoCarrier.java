package org.tracker.ubus.ubus.Components.Trips.TripTraffic.DTO.Internal;

public record TrafficInfoCarrier(String reason, int delayInMinutes) {

    public static TrafficInfoCarrier of(String reason, int delayInMinutes) {
        return new TrafficInfoCarrier(reason, delayInMinutes);
    }
}
