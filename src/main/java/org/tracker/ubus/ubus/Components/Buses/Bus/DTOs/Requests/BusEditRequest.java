package org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Requests;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record BusEditRequest(

        String id,

        String name,

        String model,

        @Pattern(regexp = "ELECTRIC|COMBUSTION",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Bus Type status is Invalid"
        )
        String type,

        @Pattern(regexp = "OPERATIONAL|MAINTENANCE|OUT OF SERVICE",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Operational status is Invalid"
        )
        String operationalStatus,

        @Pattern(regexp = "STATIONERY|LOADING PASSENGERS|ON TRIP|BREAK",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Activity status is Invalid"
        )
        String activityStatus,

        @Positive(message = "Capacity must be Positive")
        Integer capacity
) {
}
