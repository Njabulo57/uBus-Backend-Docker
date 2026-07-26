package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

@Builder
public record DriverTodayScheduleResponse(String arrivalTime, String departureTime,
                                          Destination from, Destination to, boolean isCompleted,
                                          LatLon start, LatLon end) {
}
