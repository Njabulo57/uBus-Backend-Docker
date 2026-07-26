package org.tracker.ubus.ubus.Components.Route.DTOs.Internal;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

@Builder
public record RouteSegmentKey(Route route, Destination to, Destination from) {

    public static RouteSegmentKey of(Route route, Destination from, Destination to) {
        return RouteSegmentKey.builder()
                .route(route)
                .to(to)
                .from(from)
                .build();
    }
}
