package org.tracker.ubus.ubus.Components.BusAssignment.Exceptions.Internal;

import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.InternalSystemException;

public final class BusAssignmentNotFoundException extends InternalSystemException {
    public BusAssignmentNotFoundException(String message) {
        super(message);
    }
}
