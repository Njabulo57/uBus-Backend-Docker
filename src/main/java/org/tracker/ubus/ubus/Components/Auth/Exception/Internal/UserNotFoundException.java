package org.tracker.ubus.ubus.Components.Auth.Exception.Internal;

import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.InternalSystemException;

public final class UserNotFoundException extends InternalSystemException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
