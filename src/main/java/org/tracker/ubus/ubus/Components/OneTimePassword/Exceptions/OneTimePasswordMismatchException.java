package org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions;


import lombok.Getter;

@Getter
public final class OneTimePasswordMismatchException extends IllegalArgumentException {

    private final long expiresIn;
    public OneTimePasswordMismatchException(String message, long expiresIn) {
        super(message);
        this.expiresIn = expiresIn;
    }

    public OneTimePasswordMismatchException(String message) {
        super(message);
        this.expiresIn = -1;
    }
}
