package org.tracker.ubus.ubus.Components.Reports.DTO.Request;


import lombok.Builder;
import java.time.LocalDate;

@Builder
public record DailyRouteUtilizationDTO(LocalDate date, String routeId,
    String routeName, Long passengers,
    Long scheduledTrips, Long busesUsed,
    Long availableBuses,Double utilization) {
}