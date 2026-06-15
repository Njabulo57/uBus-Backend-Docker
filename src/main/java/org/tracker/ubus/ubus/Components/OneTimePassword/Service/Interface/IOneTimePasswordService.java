package org.tracker.ubus.ubus.Components.OneTimePassword.Service.Interface;

import org.tracker.ubus.ubus.Components.Auth.Service.Interface.AuthTokenGenerationService;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Requests.OtpValidationRequest;



public interface IOneTimePasswordService extends AuthTokenGenerationService {


    boolean validateOTP(OtpValidationRequest otpValidationRequest);



}
