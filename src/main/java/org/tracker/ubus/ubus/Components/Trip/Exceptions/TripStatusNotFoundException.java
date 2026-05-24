package org.tracker.ubus.ubus.Components.Trip.Exceptions;

import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.InternalSystemException;

public final class TripStatusNotFoundException extends InternalSystemException {
    public TripStatusNotFoundException(String message) {
        super(message);
    }
}
