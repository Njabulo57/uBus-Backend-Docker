package org.tracker.ubus.ubus.Components.Reports.DTO.Request;

import lombok.Builder;

import java.time.LocalDate;


@Builder
public record UtilizationPrediction(
        Double predictedUtilization,
        Long passengers,
        Long scheduledTrips,
        Long availableBuses,
        Double modelRSquared,
        LocalDate predictionDate,
        String routeId,
        String routeName
) {
    public String getRecommendation() {
        if (predictedUtilization > 85) {
            return "CRITICAL: Fleet near capacity. Consider adding more buses or optimizing schedules.";
        } else if (predictedUtilization > 70) {
            return "WARNING: High utilization. Monitor closely and plan for potential capacity issues.";
        } else if (predictedUtilization > 50) {
            return "MODERATE: Healthy utilization. Good balance of resource usage.";
        } else {
            return "LOW: Under-utilized. Consider reallocating resources to busier routes.";
        }
    }
}