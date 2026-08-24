package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Internal;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;


public record LocationCarrier(Destination from, Destination to) {

    public static LocationCarrier of(Destination from, Destination to) {
        return new LocationCarrier(from, to);
    }
}
