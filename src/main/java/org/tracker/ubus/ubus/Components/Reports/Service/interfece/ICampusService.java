package org.tracker.ubus.ubus.Components.Reports.Service.interfece;

import org.tracker.ubus.ubus.Components.Reports.DTO.Response.Campus.CampusAnalyticsWrapper;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;

public interface ICampusService {


    CampusAnalyticsWrapper getCampusBusyness(ReportsTime reportsTime);
}
