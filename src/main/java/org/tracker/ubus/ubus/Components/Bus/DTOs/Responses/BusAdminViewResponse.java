package org.tracker.ubus.ubus.Components.Bus.DTOs.Responses;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;


@Builder
public record BusAdminViewResponse(
        UUID id,
        String busName,      // eg BUS-101
        String registrationNumber,  // eg GP 229-441
        String model,
        int capacity,
        String type,
        String operationalStatus,
        String activityStatus,
        String currentDriver,
        double mileage,
        int yearOfManufacture,
        String fuelType,
        boolean isActive,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
){}