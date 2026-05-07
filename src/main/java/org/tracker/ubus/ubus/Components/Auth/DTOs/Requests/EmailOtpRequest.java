package org.tracker.ubus.ubus.Components.Auth.DTOs.Requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailOtpRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email) {
}
