package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Request;

import jakarta.validation.constraints.NotNull;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.MaintenanceIssue;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.Priority;


public record BusConcernRequest(@NotNull(message = "priority is required") Priority priority,
                                String description,
                                @NotNull(message = "issue is required") MaintenanceIssue issue) {
}
