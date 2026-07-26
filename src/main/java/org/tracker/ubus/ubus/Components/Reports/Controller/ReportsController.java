package org.tracker.ubus.ubus.Components.Reports.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Reports.Service.impl.ReportsService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/Reports")
public class ReportsController {

    final ReportsService reportsService;

    /**
     * Gets the busiest bus route based on the provided time
     * HTTP Method: GET
     * Endpoint: /Reports/busiest-route
     *
     * @param tripRegisterCoordinates String containing
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<String> containing the busiest bus routes
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/busiest-route")
    public List<String> getBusiestRoute(@RequestBody String tripRegisterCoordinates) {
        return this.reportsService.busiestRoute(ReportsTime.valueOf(tripRegisterCoordinates));
    }

    /**
     * Gets the busiest campus based on the provided time
     * HTTP Method: GET
     * Endpoint: /Reports/busiest-campus
     *
     * @param tripRegisterCoordinates String containing
     *      *     DAY,
     *      *     WEEK,
     *      *     MONTH,
     *      *     YEAR,
     *      *     ALL_TIME
     * @return List<String> containing the busiest bus routes
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/busiest-campus")
    public List<String> getBusiestCampus(@RequestBody String tripRegisterCoordinates) {
        return this.reportsService.busiestCampuses(ReportsTime.valueOf(tripRegisterCoordinates));
    }

    /**
     * Gets the average delay based on the provided time
     * HTTP Method: GET
     * Endpoint: /Reports/average-delay
     *
     * @param tripRegisterCoordinates String containing
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return String containing the average delay
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/average-delay")
    public String getAverageDelay(@RequestBody String tripRegisterCoordinates) {
        return this.reportsService.averageDelay(ReportsTime.valueOf(tripRegisterCoordinates));
    }

    /**
     * Gets the peak travel hours based on the provided time
     * HTTP Method: GET
     * Endpoint: /Reports/peak-travel-hours
     * @param tripRegisterCoordinates String containing
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<String> containing the peak travel hours
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/peak-travel-hours")
    public List<String> getPeakTravelHours   (@RequestBody String tripRegisterCoordinates) {
        return this.reportsService.peakTravelTimes(ReportsTime.valueOf(tripRegisterCoordinates));
    }


}
