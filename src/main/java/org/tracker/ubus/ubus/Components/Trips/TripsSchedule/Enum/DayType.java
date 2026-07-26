package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Enum;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum DayType {

    WEEKDAY("Weekday"),
    WEEKEND("Weekend"),
    PUBLIC_HOLIDAY("Public Holiday");

    private final String label;

    DayType(String label) {
        this.label = label;
    }


    public static DayType fromLabel(String label) {
        return Stream.of(DayType.values())
                .filter(dayType -> dayType.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow( () -> new IllegalArgumentException("Invalid Day Type. Provided: " + label));
    }
}
