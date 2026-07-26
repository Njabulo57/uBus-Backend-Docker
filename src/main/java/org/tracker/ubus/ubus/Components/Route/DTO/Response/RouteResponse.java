package org.tracker.ubus.ubus.Components.Route.DTO.Response;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.util.List;

@Builder
public record RouteResponse(Route route, Destination fromDestination, Destination toDestination, List<LatLon> coordinates) {
}
