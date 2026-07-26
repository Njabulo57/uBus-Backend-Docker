package org.tracker.ubus.ubus.Components.OneTimePassword.Service.Interface;

import org.tracker.ubus.ubus.Components.Auth.Service.Interface.AuthTokenGenerationService;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Requests.OtpValidationRequest;

import java.util.UUID;


public interface IOneTimePasswordService extends AuthTokenGenerationService {


    boolean validateOTP(String otpValidationRequest);


    void sendOTP(UUID id);
}
