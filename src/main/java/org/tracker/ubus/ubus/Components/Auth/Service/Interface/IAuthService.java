package org.tracker.ubus.ubus.Components.Auth.Service.Interface;

import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.LoginRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.LoginSuccessfulResponse;

public interface IAuthService {

    LoginSuccessfulResponse login(LoginRequest loginRequest);

}
