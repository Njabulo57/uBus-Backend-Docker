package org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions;


import lombok.Getter;

@Getter
public final class OneTimePasswordExistsException extends IllegalArgumentException {

    private final long expiresIn;
    public OneTimePasswordExistsException(String message, long expiresIn) {
        super(message);
        this.expiresIn = expiresIn;
    }

    public OneTimePasswordExistsException(String message) {
        super(message);
        this.expiresIn = -1;
    }
}
