package org.tracker.ubus.ubus.Components.OneTimePassword.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordExpiredException;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordExistsException;
import org.tracker.ubus.ubus.GlobalExceptionHandler.ErrorResponse.ErrorResponse;

import java.time.LocalDateTime;


@Slf4j
@RestControllerAdvice
public class OneTimePasswordExceptionHandler {


    @ExceptionHandler(OneTimePasswordExistsException.class)
    public ResponseEntity<ErrorResponse> handleOtpMisMatchException(OneTimePasswordExistsException e) {

        String className = e.getClass().getSimpleName();
        String message = e.getMessage();
        long minutesLeft = e.getExpiresIn() > 0 ? e.getExpiresIn(): 0L;
        log.error("Caught {}\n.Error:{}.OPT expires in {} minutes. \n Trace:", className, message, minutesLeft, e);

        HttpStatus notFound = HttpStatus.NOT_FOUND;
        LocalDateTime nowed = LocalDateTime.now();

        ErrorResponse errorResponse = ErrorResponse.builder().message(message)
                .statusCodePhrase(notFound.getReasonPhrase())
                .statusCode(notFound.value())
                .timestamp(nowed)
                .build();


        return ResponseEntity.status(notFound)
                .body(errorResponse);
    }


    @ExceptionHandler(OneTimePasswordExpiredException.class)
    public ResponseEntity<ErrorResponse> handleOtpExpiredException(OneTimePasswordExpiredException e) {
        String className = e.getClass().getSimpleName();
        String message = e.getMessage();
        log.error("Caught {}\n.Error:{} \n Trace:", className, message, e);

        HttpStatus  gone = HttpStatus.GONE;
        LocalDateTime nowed = LocalDateTime.now();

        ErrorResponse errorResponse = ErrorResponse.builder().message(message)
                .statusCodePhrase(gone.getReasonPhrase())
                .statusCode(gone.value())
                .timestamp(nowed)
                .build();

        return ResponseEntity.status(gone)
                .body(errorResponse);
    }


}
