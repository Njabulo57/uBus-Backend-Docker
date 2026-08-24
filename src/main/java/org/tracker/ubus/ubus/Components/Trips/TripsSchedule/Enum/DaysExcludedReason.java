package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum DaysExcludedReason {


    RECESS_HOLIDAYS("Recess Holidays"),
    PUBLIC_HOLIDAYS("Public Holidays"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");


    private final String label;

    public static DaysExcludedReason fromLabel(String label) {
        return Stream.of(DaysExcludedReason.values())
                .filter( days -> days.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Days Excluded Reason. Provided: " + label));
    }

}


