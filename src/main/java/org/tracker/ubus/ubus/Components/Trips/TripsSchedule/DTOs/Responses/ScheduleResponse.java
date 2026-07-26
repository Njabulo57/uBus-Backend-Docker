package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;


@Builder
public record ScheduleResponse(
        UUID id, String busName,
        String fromDestination, String toDestination,
        String fromLabel, String toLabel,
        LocalDate serviceDate, LocalTime departureTime,
        LocalTime arrivalTime,
        String scheduleTripStatus, String dayType,
        boolean isDone
) {}