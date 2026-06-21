package org.tracker.ubus.ubus.Components.Auth.Service.Interface;

import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;

import java.util.UUID;

/**
 * Interface for generating authentication tokens for users.
 * This service is used to create time-sensitive tokens, often used for authentication
 * or verification purposes, associated with a specific user in the system.
 */
public interface AuthTokenGenerationService {


    /**
     * Generates an authentication token for the given user.
     * This method is typically used for creating time-sensitive tokens for authentication
     * or verification purposes, linked to a specific user in the system.
     *
     * @param userId the unique identifier of the user for whom the authentication token is generated.
     * @return an {@code OtpInternalCarrier} object containing the generated OTP and its expiration time.
     */
    OtpInternalCarrier generateAuthToken(UUID userId);


    /**
     * Generates an authentication token for the given email address.
     * This method is typically used to create time-sensitive tokens for authentication
     * or verification purposes, tied to a specific user identified by their email address.
     *
     * @param email the email address of the user for whom the authentication token is generated.
     * @return an {@code OtpInternalCarrier} object containing the generated OTP and its expiration time.
     */
    OtpInternalCarrier generateAuthToken(String email);
}
