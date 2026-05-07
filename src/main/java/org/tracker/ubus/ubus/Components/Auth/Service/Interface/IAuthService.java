package org.tracker.ubus.ubus.Components.Auth.Service.Interface;


import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.EmailOtpRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.LoginRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.RegisterRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.EmailOtpResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.LoginSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.RegisterSuccessfulResponse;

public interface IAuthService {

    LoginSuccessfulResponse login(LoginRequest loginRequest);

    RegisterSuccessfulResponse register(RegisterRequest registerRequest);

    EmailOtpResponse requestOtp(EmailOtpRequest emailOtpRequest);
}
