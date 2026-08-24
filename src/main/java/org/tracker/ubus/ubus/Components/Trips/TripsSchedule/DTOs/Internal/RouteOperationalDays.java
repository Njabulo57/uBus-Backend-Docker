package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Internal;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;


@Builder
public record RouteOperationalDays(Route route, String daysOfTheWeek) {


}
