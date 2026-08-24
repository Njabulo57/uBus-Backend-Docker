package org.tracker.ubus.ubus.Components.Reports.DTO.Response;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

@Builder
public record MaxPeekHoursInsight(Destination from, Destination to, String message) {
}
