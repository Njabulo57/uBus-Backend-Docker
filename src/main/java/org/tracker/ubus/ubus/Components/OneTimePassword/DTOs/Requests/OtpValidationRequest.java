package org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Requests;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record OtpValidationRequest(


        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "One Time Password Required") String otp){
}
