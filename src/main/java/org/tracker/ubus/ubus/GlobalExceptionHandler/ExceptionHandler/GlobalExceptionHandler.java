package org.tracker.ubus.ubus.GlobalExceptionHandler.ExceptionHandler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.tracker.ubus.ubus.GlobalExceptionHandler.ErrorResponse.ErrorResponse;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        String allErrorMessages = this.getAllFiledErrorMessages(ex);
        log.error("Caught MethodArgumentNotValidException. Errors[{}}] ", allErrorMessages);

        final HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        final int statusCode = badRequest.value();
        final String  statusCodePhrase = badRequest.getReasonPhrase();

        //error message fields
        LocalDateTime now = LocalDateTime.now();
        String message =this.getFirstErrorMessage(ex); //get the first error field message

        final ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(statusCode)
                .message(message)
                .timestamp(now)
                .statusCodePhrase(statusCodePhrase)
                .build();

        return ResponseEntity.status(badRequest)
                .body(errorResponse);


    }

    /**
     * handles any unspecified exceptions in the application
     * @param e the exception that was thrown
     * @return a ResponseEntity containing an ErrorResponse with the status code and message
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(Exception e) {

        String className = e.getClass().getSimpleName();
        String errorMessage = e.getMessage();
        log.error("Caught {}\n.Error:{} \n Trace:", className, errorMessage, e);


        final HttpStatus internalServerError = HttpStatus.INTERNAL_SERVER_ERROR;
        final int statusCode = internalServerError.value();
        final String  statusCodePhrase = internalServerError.getReasonPhrase();



        //error message fields
        LocalDateTime now = LocalDateTime.now();
        String message = "There was an Error in the server, please try again later.";


        final ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(statusCode)
                .message(message)
                .timestamp(now)
                .statusCodePhrase(statusCodePhrase)
                .build();

        return ResponseEntity.status(internalServerError)
                .body(errorResponse);

    }


    private String getFirstErrorMessage(MethodArgumentNotValidException ex) {

        return ex.getBindingResult().getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation failed");
    }

    private String getAllFiledErrorMessages(MethodArgumentNotValidException ex) {

        return ex.getBindingResult().getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("Validation failed");
    }

}
