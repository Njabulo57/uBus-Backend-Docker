package org.tracker.ubus.ubus.Components.Auth.Exception;

public final class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
