package org.tracker.ubus.ubus.Components.Auth.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Getter
public class AccountLockedException extends BaseAuthenticationException {


    public AccountLockedException(String message) {

      super(message, HttpStatus.UNAUTHORIZED);
    }
}
