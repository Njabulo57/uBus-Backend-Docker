package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.MaintenanceIssue;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.Priority;


public record BusConcernRequest(@NotNull(message = "priority is required") Priority priority,
                                @NotBlank(message = "description is required") String description,
                                @NotBlank(message = "issue is required") MaintenanceIssue issue) {


}
