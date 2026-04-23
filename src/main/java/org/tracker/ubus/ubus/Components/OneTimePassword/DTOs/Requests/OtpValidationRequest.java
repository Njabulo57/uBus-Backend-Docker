package org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Requests;



import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record OtpValidationRequest(
        UUID userId,
        @NotBlank(message = "One Time Password Required") String otp){
}
