package org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.PastTrip;


import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@SuperBuilder
@AllArgsConstructor
public abstract class AbstractPastTrip {

    private String route;

    private String fromCampus;
    private String toCampus;

    private boolean wasDelayed;

    private LocalDateTime departureTime;

    private LocalDateTime arrivalTime;
}
