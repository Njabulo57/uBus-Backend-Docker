package org.tracker.ubus.ubus.Components.Auth.Service.Interface;

import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;

import java.util.UUID;

/**
 * Interface for generating authentication tokens for users.
 * This service is used to create time-sensitive tokens, often used for authentication
 * or verification purposes, associated with a specific user in the system.
 */
public interface AuthTokenGenerationService {

    OtpInternalCarrier generateAuthToken(UUID userId);
}
