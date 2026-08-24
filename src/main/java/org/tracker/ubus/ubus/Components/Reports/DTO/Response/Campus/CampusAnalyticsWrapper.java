package org.tracker.ubus.ubus.Components.Reports.DTO.Response.Campus;

import lombok.Builder;

import java.util.List;

@Builder
public record CampusAnalyticsWrapper(List<CampusAnalytics> campuses) {
}
