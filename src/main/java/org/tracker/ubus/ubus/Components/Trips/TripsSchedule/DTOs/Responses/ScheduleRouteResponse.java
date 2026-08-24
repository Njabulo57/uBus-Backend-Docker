package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses;


import lombok.Builder;

@Builder
public record ScheduleRouteResponse(String busName, String busModel,
                                    String departTime, String arrivalTime,
                                    String from, String to) {
}
