package org.tracker.ubus.ubus.Components.Buses.BusPreference.Exceptions;

import org.springframework.http.HttpStatus;
import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.ExternalBusinessException;

public final class BusPreferenceMaximumException extends ExternalBusinessException {
    public BusPreferenceMaximumException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
