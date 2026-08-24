package org.tracker.ubus.ubus.Components.Reports.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.Campus.CampusAnalyticsWrapper;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Reports.Service.interfece.ICampusService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/campus-analytics")
public class CampusAnalyticsController {

    private final ICampusService campusService;

    @RequestMapping("/get-reports")
    public CampusAnalyticsWrapper getCampusAnalytics(final @RequestParam("date") ReportsTime date){
        return this.campusService.getCampusBusyness(date);
    }
}
