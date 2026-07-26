package org.tracker.ubus.ubus.Components.Reports.Service.interfece;

import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;

import java.util.List;

public interface IReportsService {
    public List<String> busiestRoute(ReportsTime reportsTime);

    public List<String> busiestCampuses(ReportsTime reportsTime);

    public String averageDelay(ReportsTime reportsTime);
    public List<String> peakTravelTimes(ReportsTime reportsTime);

}