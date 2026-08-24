package org.tracker.ubus.ubus.Components.Reports.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.Bus.BusFleetWrapper;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Reports.Service.impl.BusOperationalHistoryReportsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bus-operational-reports")
public class BusOperationalReportsController {

    private final BusOperationalHistoryReportsService busOperationalHistoryReportsService;


    @PostMapping("/get-reports")
    public BusFleetWrapper getBusFleetOperationalAnalytics(
            final @RequestParam("date") ReportsTime reportsTime){

        return busOperationalHistoryReportsService
                .getBusFleetOperationalAnalytics(reportsTime);
    }
}
