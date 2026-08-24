package org.tracker.ubus.ubus.Components.Reports.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tracker.ubus.ubus.Components.Reports.DTO.Request.ReportingFilterRequest;

import org.tracker.ubus.ubus.Components.Reports.DTO.Response.PeekResponseWrapper;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Reports.Service.interfece.IPeekReportsService;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

@RestController
@RequiredArgsConstructor
@RequestMapping("/peek-hour-reports")
public class PeekHourReportsController {

    private final IPeekReportsService peekReportsService;

    @PostMapping("/get-reports")
    public PeekResponseWrapper getReports(
            final @RequestParam("date")ReportsTime timeFrame,
            final @RequestParam("route") Route route){
        var filterRequester = ReportingFilterRequest.of(timeFrame, route);
        return peekReportsService.getHourlyPeeks(filterRequester);
    }
    //passengers/ actual usage
    //bus capacity/ understand what x passengers mean
    //occupancy percentage
    //time period
    //peek time
    //no. of times it reaches 90% threshold
}
