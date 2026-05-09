package org.tracker.ubus.ubus.Components.Auth.ExceptionHandler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.LockedAccountResponse;
import org.tracker.ubus.ubus.Components.Auth.Exception.AccountLockedException;
import org.tracker.ubus.ubus.Components.Auth.Exception.BaseAuthenticationException;
import org.tracker.ubus.ubus.Global.Exceptions.ErrorResponse.ErrorResponse;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class AuthExceptionHandler {


    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(BaseAuthenticationException ex) {
        String className = ex.getClass().getSimpleName();
        String errorMessage = ex.getMessage();

        log.error("Caught {}\n.Error:{} \n Trace:", className, errorMessage, ex);


        final HttpStatus status = ex.getStatus();

        String message = ex.getMessage();
        String statusCodePhrase = status.getReasonPhrase();
        int statusCode = status.value();
        final LocalDateTime now = LocalDateTime.now();

        final ErrorResponse response = ErrorResponse.builder()
                .message(message)
                .statusCodePhrase(statusCodePhrase)
                .statusCode(statusCode)
                .timestamp(now)
                .build();

        return ResponseEntity.status(status)
                .body(response);
    }


    @ExceptionHandler(AccountLockedException.class)
    public  ResponseEntity<LockedAccountResponse> handleAccountLockedException(AccountLockedException ex) {
        String className = ex.getClass().getSimpleName();
        String errorMessage = ex.getMessage();

        log.error("Caught {}\n.Error:{} \n Trace:", className, errorMessage, ex);


        final HttpStatus status = ex.getStatus();

        String message = ex.getMessage();
        String statusCodePhrase = status.getReasonPhrase();
        int statusCode = status.value();
        final LocalDateTime now = LocalDateTime.now();

        LockedAccountResponse lockedAccountResponse = LockedAccountResponse.builder()
                .message(message)
                .statusCodePhrase(statusCodePhrase)
                .statusCode(statusCode)
                .timestamp(now)
                .userId(null)
                .build();

        return ResponseEntity.status(status)
                .body(lockedAccountResponse);
    }

}
