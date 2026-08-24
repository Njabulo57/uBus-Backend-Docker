package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses;


import lombok.Builder;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

@Builder
public record ScheduleViewResponse(
        Destination from, Destination to,
        String departureTime, String arrivalTime,
        int busesAvailable, Route route,
        String routeLabel, String routeIdentifier
) {
}
