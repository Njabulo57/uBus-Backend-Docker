package org.tracker.ubus.ubus.Components.Users.PotentialAdmin.Exception;

import org.springframework.http.HttpStatus;
import org.tracker.ubus.ubus.GlobalExceptionHandler.Exeption.ExternalBusinessException;

public final class PendingAdminExistsException extends ExternalBusinessException {
    public PendingAdminExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
