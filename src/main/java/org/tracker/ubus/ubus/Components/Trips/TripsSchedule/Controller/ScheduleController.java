package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses.DriverTodayScheduleResponse;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses.ScheduleResponse;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses.ToDestinationResponse;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Mapper.ScheduleMapper;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Service.Interface.ITripScheduleService;

import java.util.Collection;


@RestController
@RequiredArgsConstructor
@RequestMapping("/schedules")
public class ScheduleController {

    private final ITripScheduleService scheduleService;

    @GetMapping("/get-driver-today-schedule")
    public Collection<DriverTodayScheduleResponse> getDriverTodaySchule() {
        return this.scheduleService.getDriverTodaySchedule();
    }


    @GetMapping("/get-driver-today-current-trip")
    public DriverTodayScheduleResponse getDriverCurrentScheduleTrip() {
        return this.scheduleService.getCurrentDriverScheduleTrip();
    }
    @GetMapping("/get-driver-today-next-trip")
    public DriverTodayScheduleResponse getDriverNextScheduleTrip() {
        return this.scheduleService.getNextDriverScheduleTrip();
    }

}
