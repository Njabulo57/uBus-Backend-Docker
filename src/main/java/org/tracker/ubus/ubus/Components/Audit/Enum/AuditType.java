package org.tracker.ubus.ubus.Components.Audit.Enum;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AuditType {

    ADMIN_DRIVER_APPROVAL("Driver %s %s account approved by %s"),
    ADMIN_DRIVER_BUS_ASSIGNMENT("Driver %s was assigned bus %s by %s"),
    ADMIN_DRIVER_BUS_UNASSIGNMENT("Driver %s was unassigned from bus %s by %s"),

    PENDING_ADMIN_ADDITION("Super Admin %s %s has added pending admin %s to the system"),
    PENDING_ADMIN_DELETION("Super Admin %s %s has deleted pending admin %s from the system"),
    PENDING_ADMIN_APPROVAL("Super Admin %s %s has approved pending admin %s to the system"),
    PENDING_ADMIN_EMAIL_INVITATION("Super Admin %s %s has invited %s to join the system through an invitation code"),

    ADMIN_BUS_STATUS_CHANGE("Bus %s status changed to %s by %s"),
    ADMIN_BUS_ROUTE_CHANGE("Bus %s route changed to %s by %s"),
    ADMIN_BUS_CAPACITY_CHANGE("Bus %s capacity changed to %s by %s");


    private final String message;




    public String format(Object... args) {
        return String.format(message, args);
    }


}