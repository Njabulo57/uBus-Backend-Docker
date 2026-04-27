package org.tracker.ubus.ubus.Components.Bus.Enum;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BusOperationalStatus {

    OPERATIONAL("Operational"),           // Fully operational, can be scheduled
    MAINTENANCE("Maintenance"),      // Undergoing maintenance/repair
    OUT_OF_SERVICE("Out Of Service");   // Not operational (accident, major issue)

    private final String label;

    BusOperationalStatus(String label) {
        this.label = label;
    }

    public static BusOperationalStatus fromLabel(String label)
            throws IllegalArgumentException {
        return Arrays.stream(BusOperationalStatus.values())
                .filter(busOperationalStatus -> busOperationalStatus.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Bus Operational Status Not Found. Provided: " + label));
    }


}
