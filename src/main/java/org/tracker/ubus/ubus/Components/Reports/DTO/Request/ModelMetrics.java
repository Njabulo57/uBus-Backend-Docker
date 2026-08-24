package org.tracker.ubus.ubus.Components.Reports.DTO.Request;

import lombok.Builder;


import java.util.Map;


@Builder
public record ModelMetrics(
    double rSquared, double adjustedRSquared,
    double mse, Map<String, Double> featureImportance) {
}
