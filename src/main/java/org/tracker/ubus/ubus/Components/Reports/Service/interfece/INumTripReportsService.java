package org.tracker.ubus.ubus.Components.Reports.Service.interfece;

import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.ReportsResponse;

import java.util.List;

public interface INumTripReportsService {
    public ReportsResponse numTrips(ReportsTime reportsTime);
    public List<ReportsResponse> numTripsByStatus(ReportsTime reportsTime);
    public List<ReportsResponse> numTripsByTimeIntervals(ReportsTime reportsTime);
    public List<ReportsResponse> numTripsByRouteAndStatus(ReportsTime reportsTime);

}
