package org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions;

import org.springframework.http.HttpStatus;
import org.tracker.ubus.ubus.Components.Auth.Exception.BaseAuthenticationException;

public final class OneTimePasswordMismatchException extends BaseAuthenticationException {
    public OneTimePasswordMismatchException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
