package org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractPastTrip {

    private String route;

    private String fromCampus;
    private String toCampus;

    private boolean wasDelayed;

    private LocalDateTime departureTime;

    private LocalDateTime arrivalTime;
}
