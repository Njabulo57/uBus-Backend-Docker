package org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption;

import org.springframework.http.HttpStatus;


public class ExternalBusinessException extends BaseException {

    public ExternalBusinessException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
