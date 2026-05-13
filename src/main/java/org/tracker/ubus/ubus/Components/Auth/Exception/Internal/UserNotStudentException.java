package org.tracker.ubus.ubus.Components.Auth.Exception.Internal;

import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.InternalSystemException;

public final class UserNotStudentException extends InternalSystemException {
    public UserNotStudentException(String message) {
        super(message);
    }
}
