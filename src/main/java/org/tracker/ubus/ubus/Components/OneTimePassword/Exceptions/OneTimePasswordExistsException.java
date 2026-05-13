package org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions;


import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.ExternalBusinessException;

@Getter
public final class OneTimePasswordExistsException extends ExternalBusinessException {

    private final long expiresIn;
    public OneTimePasswordExistsException(String message, long expiresIn) {
        super(message, HttpStatus.CONFLICT);
        this.expiresIn = expiresIn;
    }

    public OneTimePasswordExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
        this.expiresIn = -1;
    }
}
