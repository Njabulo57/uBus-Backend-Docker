package org.tracker.ubus.ubus.Components.Admin.DTO.Response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DriverPendingResponseDTO(UUID driverId, String firstName, String lastName,
                                       String email, String phoneNumber
) {}
