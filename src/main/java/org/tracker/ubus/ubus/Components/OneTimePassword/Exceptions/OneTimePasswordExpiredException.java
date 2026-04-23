package org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions;

public final class OneTimePasswordExpiredException extends IllegalStateException {
    public OneTimePasswordExpiredException(String message) {
        super(message);
    }
}
