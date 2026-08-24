package org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;


@Builder
public record BusCurrentNextDestinations(Destination currentDestination, Destination nextDestination) {


    public BusCurrentNextDestinations {
        if (currentDestination.equals(nextDestination))
            throw new IllegalArgumentException("Current and next destination cannot be the same");
    }


    public static BusCurrentNextDestinations of(Destination currentDestination, Destination nextDestination) {
        return BusCurrentNextDestinations.builder()
                .currentDestination(currentDestination)
                .nextDestination(nextDestination)
                .build();
    }
}
