package org.tracker.ubus.ubus.Components.Auth.Exception;


import org.springframework.http.HttpStatus;

public final class InvalidCredentialsException extends BaseAuthenticationException {

    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
