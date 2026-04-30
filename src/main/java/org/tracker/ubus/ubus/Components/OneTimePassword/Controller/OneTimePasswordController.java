package org.tracker.ubus.ubus.Components.OneTimePassword.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Requests.OtpValidationRequest;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Responses.OtpCreatedResponse;
import org.tracker.ubus.ubus.Components.OneTimePassword.Service.Interface.IOneTimePasswordService;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/one-time-password")
class OneTimePasswordController {

    private final IOneTimePasswordService oneTimePasswordService;


    @PostMapping("/validate")
    @ResponseStatus(HttpStatus.OK)
    public boolean validateOTP(@RequestBody @Valid OtpValidationRequest otpValidationRequest) {
        return oneTimePasswordService.validateOTP(otpValidationRequest);
    }

}
