package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses;

import lombok.Builder;


@Builder
public record ScheduleLegResponse(String id,
                                  String scheduleId,
                                  String to,
                                  String from,
                                  String departureTime,
                                  String arrivalTime,
                                  String dayOfTheWeek) {

}
