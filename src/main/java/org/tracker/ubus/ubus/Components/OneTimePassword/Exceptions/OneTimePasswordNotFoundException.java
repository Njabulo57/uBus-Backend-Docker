package org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions;

public final class OneTimePasswordNotFoundException extends IllegalArgumentException {
    public OneTimePasswordNotFoundException(String message) {
        super(message);
    }
}
