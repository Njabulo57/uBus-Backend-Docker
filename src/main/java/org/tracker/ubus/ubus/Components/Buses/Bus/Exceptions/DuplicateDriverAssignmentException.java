package org.tracker.ubus.ubus.Components.Buses.Bus.Exceptions;

import org.springframework.http.HttpStatus;
import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.ExternalBusinessException;

public final class DuplicateDriverAssignmentException extends ExternalBusinessException {
    public DuplicateDriverAssignmentException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
