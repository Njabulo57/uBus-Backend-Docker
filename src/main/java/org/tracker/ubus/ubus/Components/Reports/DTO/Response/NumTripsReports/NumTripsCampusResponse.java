package org.tracker.ubus.ubus.Components.Reports.DTO.Response.NumTripsReports;

import lombok.Builder;

import java.util.List;

@Builder
public record NumTripsCampusResponse(String campus,
                                     List<NumTripsValuesResponse> values) {
}
