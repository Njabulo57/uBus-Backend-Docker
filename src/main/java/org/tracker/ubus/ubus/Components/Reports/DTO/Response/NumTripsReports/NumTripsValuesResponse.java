package org.tracker.ubus.ubus.Components.Reports.DTO.Response.NumTripsReports;

import lombok.Builder;

@Builder
public record NumTripsValuesResponse(String key,
                                     int value,
                                     String completed,
                                     String incomplete,
                                     String delayed) {
}
