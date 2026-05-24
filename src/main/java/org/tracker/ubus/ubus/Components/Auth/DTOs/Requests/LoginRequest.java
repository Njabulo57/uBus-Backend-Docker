package org.tracker.ubus.ubus.Components.Auth.DTOs.Requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid Credentials")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Invalid Credentials")
        String password
) {
}