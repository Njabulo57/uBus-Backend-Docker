package org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions;

import org.springframework.http.HttpStatus;
import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.ExternalBusinessException;

public final class OneTimePasswordMismatchException extends ExternalBusinessException {
    public OneTimePasswordMismatchException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
