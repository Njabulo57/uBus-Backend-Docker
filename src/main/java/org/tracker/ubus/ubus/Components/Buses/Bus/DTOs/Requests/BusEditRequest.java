package org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record BusEditRequest(
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

        @NotBlank(message = "Activity status is required")
        @Pattern(regexp = "STATIONERY|LOADING PASSENGERS|ON TRIP|BREAK",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Activity status is Invalid"
        )
        String activityStatus,

        @NotNull(message = "Capacity is required")
        @Positive(message = "Capacity must be Positive")
        Integer capacity
) {
}
