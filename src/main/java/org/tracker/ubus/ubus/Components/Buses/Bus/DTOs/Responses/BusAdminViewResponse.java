package org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Responses;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;


@Builder
public record BusAdminViewResponse(
        UUID id,
        String busName,      // eg BUS-101
        String model,
        int capacity,
        String type,
        String registrationNumber,
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