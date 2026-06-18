package org.tracker.ubus.ubus.Components.ForgotPassword.Exceptions.External;

import org.springframework.http.HttpStatus;
import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.ExternalBusinessException;

public final class ForgotPasswordIllegalException extends ExternalBusinessException {
    public ForgotPasswordIllegalException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
