package org.tracker.ubus.ubus.Components.Auth.Controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.EmailOtpRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.LoginRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.RegisterRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.EmailOtpResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.LoginSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.RegisterSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.Service.Interface.IAuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {


    private final IAuthService authService;


    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginSuccessfulResponse login(@RequestBody @Valid final LoginRequest loginRequest)  {
        return authService.login(loginRequest);
    }

    @PostMapping("/email-otp")
    public EmailOtpResponse requestOtp(@RequestBody @Valid final EmailOtpRequest emailOtpRequest) {
        return authService.requestOtp(emailOtpRequest);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterSuccessfulResponse register(@RequestBody @Valid final RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

}
