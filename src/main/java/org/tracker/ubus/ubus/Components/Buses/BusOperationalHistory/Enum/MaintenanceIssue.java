package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MaintenanceIssue {
    ENGINE("Engine"),
    ELECTRICAL("Electrical"),
    BODY("Body"),
    TIRES("Tires"),
    OTHER("Other");

    private final String label;

    public static MaintenanceIssue fromLabel(String label) {
        var labelUpperCase = label.toUpperCase();
        return MaintenanceIssue.valueOf(labelUpperCase);
    }
}