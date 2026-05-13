package org.tracker.ubus.ubus.Components.Auth.Exception.Internal;

import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.InternalSystemException;

public final class AdminRegistrationNotAllowedException extends InternalSystemException {
    public AdminRegistrationNotAllowedException(String message) {
        super(message);
    }
}
