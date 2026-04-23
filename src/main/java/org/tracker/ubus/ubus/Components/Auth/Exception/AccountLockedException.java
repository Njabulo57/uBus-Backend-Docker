package org.tracker.ubus.ubus.Components.Auth.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Getter
public class AccountLockedException extends BaseAuthenticationException {

    private final UUID userId;
    public AccountLockedException(String message, UUID userId) {

      super(message, HttpStatus.UNAUTHORIZED);
      this.userId = userId;
    }
}
