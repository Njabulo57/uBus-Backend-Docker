package org.tracker.ubus.ubus.Components.Reports.DTO.Response.Bus;
import lombok.Builder;

import java.util.Collection;
import java.util.List;


@Builder
public record BusFleetWrapper(int totalBuses, int availableBuses,
                              int inCriticalCondition, String mostCommonIssue,
                              List<String> top3Issues, Collection<BusOperationalResponse> operationalStatus) {

}