package org.tracker.ubus.ubus.Components.Trips.Trip.Exceptions;

import org.springframework.http.HttpStatus;
import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.ExternalBusinessException;

public final class TripNotFoundException extends ExternalBusinessException {
    public TripNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
