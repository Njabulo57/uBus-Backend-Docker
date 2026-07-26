package org.tracker.ubus.ubus.Components.Trips.TripCalendarRule.Enum;

import lombok.Getter;

import java.util.stream.Stream;


@Getter
public enum CalendarRuleType {

    RECESS("Recess"),
    SATURDAY("Saturday"),
    STUDY_BREAK("Study Break"),

    PUBLIC_HOLIDAY("Public Holiday");

    private final String label;

    CalendarRuleType(String label) {
        this.label = label;
    }

    public static CalendarRuleType fromLabel(String label) {
        return Stream.of(CalendarRuleType.values())
                .filter(calendarRuleType -> calendarRuleType.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow( () -> new IllegalArgumentException("Invalid Calendar Rule Type. Provided: " + label));

    }

}
