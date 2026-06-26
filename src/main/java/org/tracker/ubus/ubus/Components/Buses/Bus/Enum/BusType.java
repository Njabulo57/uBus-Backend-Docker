package org.tracker.ubus.ubus.Components.Buses.Bus.Enum;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BusType {

    ELECTRIC("Electric", "UJ EV BUS"),
    COMBUSTION("Combustion", "STABUS");

    private final String label;
    private final String description;

    BusType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public static BusType fromLabel(String value) {
        return  Arrays.stream(BusType.values())
                .filter(busType -> busType.label.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Bus Type. Provided: " + value));
    }

}
