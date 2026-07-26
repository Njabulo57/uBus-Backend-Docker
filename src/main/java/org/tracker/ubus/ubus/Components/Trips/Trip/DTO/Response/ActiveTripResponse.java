package org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response;

import lombok.Builder;

import java.time.LocalTime;
import java.util.UUID;


@Builder
public record ActiveTripResponse(UUID id, UUID busId,
                                 String route, String busName, String busStatus,
                                 String from, String to,

                                 LocalTime eta,
                                 double fromLatitude, double fromLongitude,
                                 double toLatitude, double toLongitude,
                                 double[][] routeCoordinates,
                                 String driverName) {
}
