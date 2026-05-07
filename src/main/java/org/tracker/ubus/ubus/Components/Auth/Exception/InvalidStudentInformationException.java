package org.tracker.ubus.ubus.Components.Auth.Exception;

import org.springframework.http.HttpStatus;

public final class InvalidStudentInformationException extends BaseAuthenticationException {
    public InvalidStudentInformationException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
