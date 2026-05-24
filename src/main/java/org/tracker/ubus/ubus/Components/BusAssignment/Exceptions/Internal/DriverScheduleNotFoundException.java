package org.tracker.ubus.ubus.Components.BusAssignment.Exceptions.Internal;

import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.InternalSystemException;

public class DriverScheduleNotFoundException extends InternalSystemException {
    public DriverScheduleNotFoundException(String message) {
        super(message);
    }
}
