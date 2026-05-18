package org.tracker.ubus.ubus.Components.Auth.Exception.Internal;

import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.InternalSystemException;

public final class UserRoleNotAllowedException extends InternalSystemException {
    public UserRoleNotAllowedException(String message) {
        super(message);
    }
}
