package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Internal.LocationCarrier;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Internal.TimeRangeCarrier;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Requests.ScheduleLegRequest;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Requests.ScheduleRequest;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Requests.ScheduleScheduleLegRequest;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses.*;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Service.Interface.ITripScheduleService;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/schedules")
public class ScheduleController {

    private final ITripScheduleService scheduleService;


    @GetMapping("/get-driver-today-schedule")
    public Collection<DriverTodayScheduleResponse> getDriverTodaySchule() {
        return this.scheduleService.getDriverTodaySchedule();
    }


    @GetMapping("/get-shcedule-for-view")
    public ScheduleViewWrapper getDriverTodayScheduleForView(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size,
            @RequestParam(required = false) final Destination from,
            @RequestParam(required = false) final DayOfWeek dayOfWeek,
            @RequestParam(required = false) final Destination to,
            @RequestParam(required = false, defaultValue = "04:00") final LocalTime fromDate
            ) {

        var pageable = Pageable.ofSize(size)
                .withPage(page);
        var locationCarrier = LocationCarrier.of(from, to);
        var timeRangeCarrier = TimeRangeCarrier.of(fromDate, dayOfWeek);
        return this.scheduleService.getScheduleView(pageable, locationCarrier,timeRangeCarrier);
    }


    @GetMapping("/get-driver-today-current-trip")
    public DriverTodayScheduleResponse getDriverCurrentScheduleTrip() {
        return this.scheduleService.getCurrentDriverScheduleTrip();
    }


    @GetMapping("/get-driver-today-next-trip")
    public DriverTodayScheduleResponse getDriverNextScheduleTrip() {
        return this.scheduleService.getNextDriverScheduleTrip();
    }


    @GetMapping("/get-schedule-base-information")
    public Collection<ScheduleBaseInformation> getScheduleBaseInformation() {
        return  this.scheduleService.getScheduleBaseInformation();
    }

    @GetMapping("/get-schedule-informaton-by-route")
    public Collection<ScheduleRouteWrapperResponse> getScheduleRouteWrapperResponse(
            @RequestParam("route") final String route) {
        return this.scheduleService.getScheduleRouteWrapperResponse(route);
    }

    /**
     * method to add schedules
     * @param ScheduleRequest containing
     *                          route, validFromDate(yyyy-mm-dd), validToDate(yyyy-mm-dd)
     * @return ScheduleObjectResponse
     */
    @PostMapping("/addSchedule")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleObjectResponse addSchedule(@RequestBody ScheduleRequest ScheduleRequest) {
        return this.scheduleService.addSchedule(ScheduleRequest);
    }

    /**
     * method to remove schedules
     * @param ScheduleRequest containing
     *                        id
     */
    @DeleteMapping("/removeSchedule")
    @ResponseStatus(HttpStatus.OK)
    public void removeSchedule(@RequestBody ScheduleRequest ScheduleRequest) {
        this.scheduleService.removeSchedule(ScheduleRequest);
    }

    /**
     * method to update schedules
     * @param ScheduleRequest containing
     *                        route, validFromDate(yyyy-mm-dd), validToDate(yyyy-mm-dd)
     * @return ScheduleObjectResponse
     */
    @PutMapping("/updateSchedule")
    @ResponseStatus(HttpStatus.OK)
    public ScheduleObjectResponse updateSchedule(@RequestBody ScheduleRequest ScheduleRequest) {
        return this.scheduleService.updateSchedule(ScheduleRequest);
    }

    /**
     * method to get schedules legs
     * @return Collection of ScheduleLegResponses
     */
     @GetMapping("/getScheduleLegs")
     public List<ScheduleLegResponse> getScheduleLegs() {
         return this.scheduleService.getScheduleLegs();
     }

    /**
     * method to get schedules
     * @return Collection of ScheduleObjectResponses
     */
    @GetMapping("/getSchedules")
    public List<ScheduleObjectResponse> getSchedules() {
        return this.scheduleService.getSchedules();
    }

    /**
     * method to add schedules legs
     * @param ScheduleLegRequest containing
     *                           to(APK,APB,DFC,SWC,JBS),
     *                           from(APK,APB,DFC,SWC,JBS),
     *                           departureTime(hh:mm),
     *                           arrivalTime(hh:mm),
     *                           dayOfTheWeek(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
     *
     * @return ScheduleLegResponse
     */
    @PostMapping("/addScheduleLeg")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleLegResponse addScheduleLeg(@RequestBody ScheduleLegRequest ScheduleLegRequest) {
        return this.scheduleService.addScheduleLeg(ScheduleLegRequest);
    }

    /**
     * method to remove schedules legs
     * @param ScheduleLegRequest containing
     *                           id
     */
    @DeleteMapping("/removeScheduleLeg")
    @ResponseStatus(HttpStatus.OK)
    public void removeScheduleLeg(@RequestBody ScheduleLegRequest ScheduleLegRequest) {
        this.scheduleService.removeScheduleLeg(ScheduleLegRequest);
    }

    /**
     * method to update schedules legs
     * @param ScheduleLegRequest containing
     *                           to(APK,APB,DFC,SWC,JBS),
     *                           from(APK,APB,DFC,SWC,JBS),
     *                           departureTime(hh:mm),
     *                           arrivalTime(hh:mm),
     *                           dayOfTheWeek(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
     * @return ScheduleLegResponse
     */
    @PutMapping("/updateScheduleLeg")
    @ResponseStatus(HttpStatus.OK)
    public ScheduleLegResponse updateScheduleLeg(@RequestBody ScheduleLegRequest ScheduleLegRequest) {
        return this.scheduleService.updateScheduleLeg(ScheduleLegRequest);
    }

    /**
     * method to add schedule and its legs
     * @param scheduleScheduleLegRequest containing
     *                           ScheduleRequest
     *                              (route, validFromDate(yyyy-mm-dd), validToDate(yyyy-mm-dd))
     *                           and list of ScheduleLegRequests
     *                              (to(APK,APB,DFC,SWC,JBS),
     *                              from(APK,APB,DFC,SWC,JBS),
     *                              departureTime(hh:mm),
     *                              arrivalTime(hh:mm),
     *                              dayOfTheWeek(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY))
     *
     * @return ScheduleLegResponse
     */
    @PostMapping("/addScheduleAndLegs")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleScheduleLegResponse addScheduleLeg(@RequestBody ScheduleScheduleLegRequest scheduleScheduleLegRequest) {
        return this.scheduleService.addScheduleScheduleLeg(scheduleScheduleLegRequest);
    }

    /**
     * method to update schedule and its legs
     * @param scheduleScheduleLegRequest containing
     *                           ScheduleRequest
     *                           and list of ScheduleLegRequests
     *
     * @return ScheduleLegResponse
     */
    @PutMapping("/updateScheduleAndLegs")
    @ResponseStatus(HttpStatus.OK)
    public ScheduleScheduleLegResponse updateScheduleLeg(@RequestBody ScheduleScheduleLegRequest scheduleScheduleLegRequest) {
        return this.scheduleService.updateScheduleScheduleLeg(scheduleScheduleLegRequest);
    }

}
