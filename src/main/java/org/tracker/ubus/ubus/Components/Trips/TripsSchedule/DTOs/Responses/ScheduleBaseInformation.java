package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses;

import lombok.Builder;

@Builder
public record ScheduleBaseInformation(String route, String destinations, String serviceDays, String effectivePeriod) {
}
