package org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption;

import org.springframework.http.HttpStatus;



public class InternalSystemException extends BaseException {

    public InternalSystemException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
