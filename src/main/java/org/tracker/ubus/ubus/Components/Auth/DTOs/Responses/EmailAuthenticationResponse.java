package org.tracker.ubus.ubus.Components.Auth.DTOs.Responses;

import java.time.LocalDateTime;

public record EmailAuthenticationResponse(
        String message,LocalDateTime createdAt) {
}
