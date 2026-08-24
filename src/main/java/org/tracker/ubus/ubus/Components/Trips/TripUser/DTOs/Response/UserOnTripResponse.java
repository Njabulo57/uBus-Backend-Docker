package org.tracker.ubus.ubus.Components.Trips.TripUser.DTOs.Response;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;


@Builder
public record UserOnTripResponse(Destination from, Destination to,
                                 boolean isCompleted,
                                 String proximityMessage,
                                 DelayStatus delayStatus) {
}
