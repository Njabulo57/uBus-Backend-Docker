package org.tracker.ubus.ubus.Components.Bus.ExceptionHandler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.tracker.ubus.ubus.Components.Bus.Exceptions.BusAlreadyExistsException;
import org.tracker.ubus.ubus.Components.Bus.Exceptions.BusInformationMismatchException;
import org.tracker.ubus.ubus.GlobalExceptionHandler.ErrorResponse.ErrorResponse;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class BusExceptionHandler {

    @ExceptionHandler(BusAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBusAlreadyException(BusAlreadyExistsException ex) {

        String className = ex.getClass().getSimpleName();
        String message = ex.getMessage();

        log.error("Caught {}. Error:{}", className, message);
        HttpStatus httpStatus = HttpStatus.CONFLICT;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Bus Already Exists")
                .statusCodePhrase(httpStatus.getReasonPhrase())
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .build();
        return  ResponseEntity.status(httpStatus)
                .body(errorResponse);
    }

    @ExceptionHandler(BusInformationMismatchException.class)
    public ResponseEntity<ErrorResponse> handleBusInformationMismatchException(BusInformationMismatchException ex) {

        String className = ex.getClass().getSimpleName();
        String message = ex.getMessage();
        log.error("Caught {}. Error:{}", className, message);

        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;


        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCodePhrase(httpStatus.getReasonPhrase())
                .statusCode(httpStatus.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(httpStatus)
                .body(errorResponse);
    }
}

