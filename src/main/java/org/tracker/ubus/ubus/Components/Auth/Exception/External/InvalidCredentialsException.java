package org.tracker.ubus.ubus.Components.Auth.Exception.External;


import org.springframework.http.HttpStatus;
import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.ExternalBusinessException;

public final class InvalidCredentialsException extends ExternalBusinessException {

    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
