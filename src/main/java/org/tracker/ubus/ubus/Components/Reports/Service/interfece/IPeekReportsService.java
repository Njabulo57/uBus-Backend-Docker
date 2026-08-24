package org.tracker.ubus.ubus.Components.Reports.Service.interfece;

import org.tracker.ubus.ubus.Components.Reports.DTO.Request.ReportingFilterRequest;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.PeekResponseWrapper;

public interface IPeekReportsService {

    PeekResponseWrapper getHourlyPeeks(ReportingFilterRequest request);
}
