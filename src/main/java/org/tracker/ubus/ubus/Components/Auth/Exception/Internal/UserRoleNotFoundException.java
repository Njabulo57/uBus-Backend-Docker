package org.tracker.ubus.ubus.Components.Auth.Exception.Internal;

import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.InternalSystemException;

public final class UserRoleNotFoundException extends InternalSystemException {
    public UserRoleNotFoundException(String message) {
        super(message);
    }
}
