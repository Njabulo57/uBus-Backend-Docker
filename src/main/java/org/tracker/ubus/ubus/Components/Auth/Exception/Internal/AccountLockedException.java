package org.tracker.ubus.ubus.Components.Auth.Exception.Internal;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.ExternalBusinessException;

@Getter
public final class AccountLockedException extends ExternalBusinessException {

    public AccountLockedException(String message) {
      super(message, HttpStatus.LOCKED);
    }
}
