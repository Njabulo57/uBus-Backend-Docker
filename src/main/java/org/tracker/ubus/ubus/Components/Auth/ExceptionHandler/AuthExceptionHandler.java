package org.tracker.ubus.ubus.Components.Auth.ExceptionHandler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.tracker.ubus.ubus.Global.Exceptions.ErrorResponse.ErrorResponse;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class AuthExceptionHandler {


    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {

        String className = ex.getClass().getSimpleName();
        String errorMessage = ex.getMessage();

        log.error("Caught {}: {}", className, errorMessage);


        final HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;

        String message = "Invalid Credentials.Try Again";
        String statusCodePhrase = unauthorized.getReasonPhrase();
        int statusCode = unauthorized.value();
        final LocalDateTime now = LocalDateTime.now();

        final ErrorResponse response = ErrorResponse.builder()
                .message(message)
                .statusCodePhrase(statusCodePhrase)
                .statusCode(statusCode)
                .timestamp(now)
                .build();

        return ResponseEntity.status(unauthorized)
                .body(response);

    }



    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(BadCredentialsException ex) {

        String className = ex.getClass().getSimpleName();
        String  errorMessage = ex.getMessage();
        log.error("Caught {}: {}", className, errorMessage);


        final HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;

        String message = "Invalid Credentials.Try Again";
        String statusCodePhrase = unauthorized.getReasonPhrase();
        int statusCode = unauthorized.value();
        final LocalDateTime now = LocalDateTime.now();

        final ErrorResponse response = ErrorResponse.builder()
                .message(message)
                .statusCodePhrase(statusCodePhrase)
                .statusCode(statusCode)
                .timestamp(now)
                .build();

        return ResponseEntity.status(unauthorized)
                .body(response);

    }
}
