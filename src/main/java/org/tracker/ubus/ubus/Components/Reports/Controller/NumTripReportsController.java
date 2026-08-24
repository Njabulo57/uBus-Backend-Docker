package org.tracker.ubus.ubus.Components.Reports.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Reports.DTO.Request.ReportsRequest;
import org.tracker.ubus.ubus.Components.Reports.Service.interfece.INumTripReportsService;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.ReportsResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/numTripReports")
public class NumTripReportsController {
    private final INumTripReportsService numTripReportsService;

    /**
     * Gets the number of trips by status based on the provided time
     * HTTP Method: GET
     * Endpoint: /Reports/num-trips-by-status
     *
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<ReportsResponce> containing the number of trips (String key,
     *                               double value,
     *                               String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-trips-by-status")
    public List<ReportsResponse> getNumTripsByStatus(@RequestBody ReportsRequest reportsRequest) {
        return this.numTripReportsService.numTripsByStatus(reportsRequest.timeFrame());
    }

    /**
     * Gets the number of trips by route and status based on the provided time
     * HTTP Method: GET
     * Endpoint: /Reports/num-trips-by-route-and-status
     *
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<ReportsResponce> containing the number of trips (String key,
     *                               double value,
     *                               String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-trips-by-route-and-status")
    public List<ReportsResponse> getNumTripsByRouteAndStatus(@RequestBody ReportsRequest reportsRequest) {
        return this.numTripReportsService.numTripsByRouteAndStatus(reportsRequest.timeFrame());
    }

    /**
     * Gets the number of trips by time intervals based on the provided time
     * HTTP Method: GET
     * Endpoint: /Reports/num-trips-by-time-intervals
     *
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<ReportsResponce> containing the number of trips (String key,
     *                               double value,
     *                               String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-trips-by-time-intervals")
    public List<ReportsResponse> getNumTripsByTimeIntervals(@RequestBody ReportsRequest reportsRequest) {
        return this.numTripReportsService.numTripsByTimeIntervals(reportsRequest.timeFrame());
    }

    /**
     * Gets the number of trips based on the provided time
     * HTTP Method: GET
     * Endpoint: /Reports/num-trips
     *
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<ReportsResponce> containing the number of trips (String key,
     *                               double value,
     *                               String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-trips")
    public ReportsResponse getNumTrips(@RequestBody ReportsRequest reportsRequest) {
        return this.numTripReportsService.numTrips(reportsRequest.timeFrame());
    }

}
