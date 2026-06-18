package org.tracker.ubus.ubus.Components.Trips.Trip.Enum;

import lombok.Getter;
import org.tracker.ubus.ubus.Components.Trips.Trip.Exceptions.TripStatusNotFoundException;

import java.util.stream.Stream;

@Getter
public enum TripStatus {

    IN_PROGRESS("In Progress"),
    COMPLETE("Complete");

    private final String label;

    TripStatus(String label) {
        this.label = label;
    }


    public TripStatus fromLabel(String label) {
        return Stream.of(TripStatus.values())
                .filter(t -> t.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new
                        TripStatusNotFoundException("Trip Status Not Valid. Provided " + label)
                );
    }

}
