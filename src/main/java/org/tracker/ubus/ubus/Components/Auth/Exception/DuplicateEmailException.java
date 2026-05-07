package org.tracker.ubus.ubus.Components.Auth.Exception;

import org.springframework.http.HttpStatus;

public final class DuplicateEmailException extends BaseAuthenticationException {

    public DuplicateEmailException(String message) {
        super(message,  HttpStatus.CONFLICT);
    }
}
