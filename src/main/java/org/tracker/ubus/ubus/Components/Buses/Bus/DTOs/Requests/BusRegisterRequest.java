package org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

import java.util.Collection;
import java.util.UUID;

public record BusRegisterRequest(

        @NotBlank(message = "Bus name is required")
        String name,

        @NotBlank(message = "Bus model is required")
        String model,

        @NotBlank(message = "Bus type is required")
        @Pattern(regexp = "ELECTRIC|COMBUSTION",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Bus Type status is Invalid"
        )
        String type,

        @NotBlank(message = "Operational status is required")
        @Pattern(regexp = "OPERATIONAL|MAINTENANCE|OUT OF SERVICE",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Operational status is Invalid"
        )
        String operationalStatus,

        @NotBlank(message = "Registration number is required")
        String registrationNumber,

        @NotNull(message = "Capacity is required")
        @Positive(message = "Capacity must be Positive")
        Integer capacity,

        @Nullable
        @Size(max = 2, message = "Drivers cannot exceed 2")
        Collection<UUID> driverIds,

        UUID busId
) {

}