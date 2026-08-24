package org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

@Builder
public record ReportsResponse(String key,
                              double value,
                              String valueUnits) {

}
