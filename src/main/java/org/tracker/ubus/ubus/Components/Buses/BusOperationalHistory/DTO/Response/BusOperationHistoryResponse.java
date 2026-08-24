package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.MaintenanceIssue;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.Priority;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record BusOperationHistoryResponse(String message, String description,
                                          String busName, LocalDate date,
                                          UUID busId, Priority priority, MaintenanceIssue issue) {
}
