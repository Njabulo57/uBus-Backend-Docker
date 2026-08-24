package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Requests;

import java.util.List;

public record ScheduleScheduleLegRequest(ScheduleRequest scheduleRequest,
                                         List<ScheduleLegRequest> scheduleLegRequests) {
}
