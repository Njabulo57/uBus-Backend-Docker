package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Enum;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleTripStatus {

    DEPARTURE("Departure"),
    ARRIVAL("Arrival");

    private final String label;


}
