package org.tracker.ubus.ubus.Components.Buses.BusAssignment.Enum;

import lombok.Getter;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Exceptions.Internal.DriverScheduleNotFoundException;

import java.util.stream.Stream;

@Getter
public enum DriverSchedule {

    MORNING_AFTERNOON("Morning To Afternoon"),
    AFTERNOON_EVENING("Afternoon To Evening");


    private final String label;


    DriverSchedule(String label) {
        this.label = label;
    }

    public static DriverSchedule fromLabel(String label) {
        return Stream.of(DriverSchedule.values())
                .filter(driverSchedule -> driverSchedule.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new DriverScheduleNotFoundException("DriverSchedule not found. Provided label: " + label));

    }
}
