package org.tracker.ubus.ubus.Components.Auth.DTOs.Responses;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmailOtpResponse(
        String message,LocalDateTime createdAt) {
}
