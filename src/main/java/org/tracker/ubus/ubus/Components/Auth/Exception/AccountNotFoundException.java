package org.tracker.ubus.ubus.Components.Auth.Exception;

import org.springframework.http.HttpStatus;

public final class AccountNotFoundException extends BaseAuthenticationException {
    public AccountNotFoundException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
