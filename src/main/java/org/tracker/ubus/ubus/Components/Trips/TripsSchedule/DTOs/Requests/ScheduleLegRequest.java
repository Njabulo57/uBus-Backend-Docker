package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Requests;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record ScheduleLegRequest(UUID id,
                                 UUID scheduleId,
                                 Destination to,
                                 Destination from,
                                 LocalTime departureTime,

                                 LocalTime arrivalTime,
                                 DayOfWeek dayOfTheWeek) {

}
