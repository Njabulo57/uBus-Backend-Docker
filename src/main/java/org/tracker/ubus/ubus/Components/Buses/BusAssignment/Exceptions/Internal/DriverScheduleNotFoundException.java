package org.tracker.ubus.ubus.Components.Buses.BusAssignment.Exceptions.Internal;

import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.InternalSystemException;

public final class DriverScheduleNotFoundException extends InternalSystemException {
    public DriverScheduleNotFoundException(String message) {
        super(message);
    }
}
