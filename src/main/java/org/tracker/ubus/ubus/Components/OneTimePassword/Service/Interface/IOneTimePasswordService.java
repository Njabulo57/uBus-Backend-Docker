package org.tracker.ubus.ubus.Components.OneTimePassword.Service.Interface;

import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Requests.OtpValidationRequest;

import java.util.UUID;

public interface IOneTimePasswordService {



    OtpInternalCarrier generateOTP(UUID userId);

    boolean validateOTP(OtpValidationRequest otpValidationRequest);



}
