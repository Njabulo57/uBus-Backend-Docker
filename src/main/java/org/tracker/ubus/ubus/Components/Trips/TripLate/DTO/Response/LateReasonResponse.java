package org.tracker.ubus.ubus.Components.Trips.TripLate.DTO.Response;

import lombok.Builder;


@Builder
public record LateReasonResponse(String reason, String message,
                                 int delayMinutes,
                                 String lateArrivalTime) {
}
