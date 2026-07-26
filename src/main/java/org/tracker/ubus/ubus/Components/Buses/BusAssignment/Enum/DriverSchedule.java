package org.tracker.ubus.ubus.Components.Buses.BusAssignment.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Exceptions.Internal.DriverScheduleNotFoundException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum DriverSchedule {

    MORNING_AFTERNOON("Morning To Afternoon",
            LocalTime.of(6, 10), LocalTime.of(17, 40)),
    AFTERNOON_EVENING("Afternoon To Evening",
            LocalTime.of(17, 40), LocalTime.of(22, 50));


    private final String label;
    private final LocalTime startTime;
    private final LocalTime endTime;



    public static DriverSchedule fromLabel(String label) {
        return Stream.of(DriverSchedule.values())
                .filter(driverSchedule -> driverSchedule.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new DriverScheduleNotFoundException("DriverSchedule not found. Provided label: " + label));

    }

    @Override
    public String toString() {
        var formatter = DateTimeFormatter.ofPattern("HH:mm"); // formats as eg(13:40)
        var startTime = this.startTime.format(formatter);
        var endTime = this.endTime.format(formatter);

        return this.label + ": " + startTime + " - " + endTime;
    }
}
