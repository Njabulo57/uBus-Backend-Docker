package org.tracker.ubus.ubus.Components.Reports.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.DriverAttendanceWrapper;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Reports.Service.impl.DriverAttendanceService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/driver-attendace")
public class DriverAttendanceController {

    private final DriverAttendanceService driverAttendanceService;

    @PostMapping("/get-drivers-attendance")
    public DriverAttendanceWrapper getDriversAttendance(final @Param("date") ReportsTime date) {
        return driverAttendanceService.getDriversAttendance(date);
    }
}
