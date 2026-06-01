package org.tracker.ubus.ubus.Components.Trips.Trip.Exceptions;

import org.springframework.http.HttpStatus;
import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.ExternalBusinessException;

public final class DriverOutSideCampusBoundsException extends ExternalBusinessException {
    public DriverOutSideCampusBoundsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
