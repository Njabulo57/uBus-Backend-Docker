package org.tracker.ubus.ubus.Components.Trip.Exceptions;

import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.InternalSystemException;

public final class TripRouteNotFoundException extends InternalSystemException {
    public TripRouteNotFoundException(String message) {
        super(message);
    }
}
