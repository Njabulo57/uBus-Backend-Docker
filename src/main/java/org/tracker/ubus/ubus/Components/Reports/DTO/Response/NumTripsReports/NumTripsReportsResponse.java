package org.tracker.ubus.ubus.Components.Reports.DTO.Response.NumTripsReports;

import lombok.Builder;

import java.util.List;

@Builder
public record NumTripsReportsResponse(List<NumTripsCampusResponse> campuses,
                                      String filteredBy,
                                      int complete,
                                      int incomplete,
                                      int delayed) {
}
