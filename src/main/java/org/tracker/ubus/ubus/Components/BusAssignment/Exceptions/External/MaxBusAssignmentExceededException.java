package org.tracker.ubus.ubus.Components.BusAssignment.Exceptions.External;

import org.springframework.http.HttpStatus;
import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.ExternalBusinessException;

public class MaxBusAssignmentExceededException extends ExternalBusinessException {
    public MaxBusAssignmentExceededException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
