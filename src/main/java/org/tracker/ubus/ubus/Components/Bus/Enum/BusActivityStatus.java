package org.tracker.ubus.ubus.Components.Bus.Enum;

import lombok.Getter;
import java.util.stream.Stream;

@Getter
public enum BusActivityStatus {

    STATIONERY("Stationery"),
    LOADING_PASSENGERS("Loading Passengers"),        // Ready for dispatch/passengers
    ON_TRIP("On Trip"),          // En route with passengers
    BREAK("Break");        // Driver break

    private final String label;

    BusActivityStatus(String label) {
        this.label = label;
    }

    public static BusActivityStatus fromLabel(String label)
            throws IllegalArgumentException {
       return Stream.of(BusActivityStatus.values())
               .filter(activity -> activity.label.equalsIgnoreCase(label))
               .findFirst()
               .orElseThrow(() -> new IllegalArgumentException("No such Activity Status.Provided: " + label));

    }

}