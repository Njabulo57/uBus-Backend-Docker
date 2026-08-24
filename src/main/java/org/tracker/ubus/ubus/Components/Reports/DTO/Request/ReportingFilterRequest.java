package org.tracker.ubus.ubus.Components.Reports.DTO.Request;

import jakarta.validation.constraints.NotNull;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;


public record ReportingFilterRequest(
        @NotNull(message = "time frame needs to be specified") ReportsTime timeFrame,
        @NotNull(message = "route needs to be specified") Route route) {

    public ReportingFilterRequest {
        if (timeFrame == null)
            throw new IllegalArgumentException("timeFrame cannot be null");

        if (route == null)
            throw new IllegalArgumentException("route cannot be null");
    }


    public static ReportingFilterRequest of(ReportsTime reportsTime, Route route) {
        return new ReportingFilterRequest(reportsTime, route);
    }
}
