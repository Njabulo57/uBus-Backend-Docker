package org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Requests;


import jakarta.validation.constraints.NotBlank;

public record OtpValidationRequest(

        @NotBlank(message = "One Time Password Required") String otp){
}
