package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Priority {

    MINOR("Minor"),
    MAJOR("Major"),
    CRITICAL("Critical");

    private final String label;

    public static Priority fromLabel(String label) {
        var labelUpperCase = label.toUpperCase();
        return Priority.valueOf(labelUpperCase);
    }
}
