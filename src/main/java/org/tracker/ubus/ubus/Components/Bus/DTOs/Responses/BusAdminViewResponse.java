package org.tracker.ubus.ubus.Components.Bus.DTOs.Responses;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BusAdminViewResponse(String name, String model,
                                   String type, String operationalStatus,
                                   String activityStatus, int capacity,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
}
