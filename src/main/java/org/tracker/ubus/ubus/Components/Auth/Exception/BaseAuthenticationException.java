package org.tracker.ubus.ubus.Components.Auth.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;


@Getter
public class BaseAuthenticationException extends AuthenticationException {

    private final HttpStatus status;
    public BaseAuthenticationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
