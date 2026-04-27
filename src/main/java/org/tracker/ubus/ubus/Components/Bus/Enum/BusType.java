package org.tracker.ubus.ubus.Components.Bus.Enum;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BusType {

    ELECTRIC("Electric"),
    COMBUSTION("Combustion");

    private final String label;

    BusType(String label) {
        this.label = label;
    }

    public static BusType fromLabel(String value) {
        return  Arrays.stream(BusType.values())
                .filter(busType -> busType.label.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Bus Type. Provided: " + value));
    }

}
