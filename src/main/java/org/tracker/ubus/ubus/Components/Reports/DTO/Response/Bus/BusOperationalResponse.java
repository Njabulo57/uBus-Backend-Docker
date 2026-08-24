package org.tracker.ubus.ubus.Components.Reports.DTO.Response.Bus;

import lombok.Builder;

@Builder
public record BusOperationalResponse(String busName, String busType,
                                     int openIssues, int openCriticalIssues,
                                     String topIssue, String lastReported,
                                     String operationalStatus,
                                     String action, int healthScore) {

}
