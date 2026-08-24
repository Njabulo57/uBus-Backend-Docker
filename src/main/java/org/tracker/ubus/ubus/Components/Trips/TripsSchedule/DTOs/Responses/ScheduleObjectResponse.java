package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record ScheduleObjectResponse(String id,
                              String route,
                              String validFromDate,
                              String validToDate) {

}
