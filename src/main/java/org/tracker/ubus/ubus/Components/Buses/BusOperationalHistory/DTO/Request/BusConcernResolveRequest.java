package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Request;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.MaintenanceIssue;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.Priority;

@Builder
public record BusConcernResolveRequest(@NotNull(message = "issue is required") MaintenanceIssue issue,
                                       @NotNull(message = "priority") Priority priority) {
}
