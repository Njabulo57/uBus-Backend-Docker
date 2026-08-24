package org.tracker.ubus.ubus.Components.Reports.DTO.Response.Campus;

import lombok.Builder;

@Builder
public record CampusUtilizationResponse(String campus, double averageUtilization,
                                        double peakUtilization, String[] peakTime,
                                        int timesExceededThreshold, String utilizationStatus) {
}
