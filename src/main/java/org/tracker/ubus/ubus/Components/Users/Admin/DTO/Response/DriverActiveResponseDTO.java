package org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DriverActiveResponseDTO(UUID driverId, String firstName,
                                      String lastName, String email, String phoneNumber) {
}
