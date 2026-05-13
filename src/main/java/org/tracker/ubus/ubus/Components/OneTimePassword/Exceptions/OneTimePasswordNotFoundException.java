package org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions;

import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.InternalSystemException;

public final class OneTimePasswordNotFoundException extends InternalSystemException {
    public OneTimePasswordNotFoundException(String message) {
        super(message);
    }
}
