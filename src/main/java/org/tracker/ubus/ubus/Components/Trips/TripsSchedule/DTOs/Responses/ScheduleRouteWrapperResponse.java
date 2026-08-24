package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses;

import lombok.Builder;

import java.util.Collection;

@Builder
public record ScheduleRouteWrapperResponse(String shift, String startTimestamp,
                                           String endTimestamp, Collection<ScheduleRouteResponse> scheduleRows) {

}
