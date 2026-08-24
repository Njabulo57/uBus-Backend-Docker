package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses;

import lombok.Builder;

import java.util.List;

@Builder
public record ScheduleScheduleLegResponse(ScheduleObjectResponse scheduleObjectResponse,
                                          List<ScheduleLegResponse> scheduleLegResponses) {
}
