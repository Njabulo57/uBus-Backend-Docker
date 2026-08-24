package org.tracker.ubus.ubus.Components.Reports.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Reports.DTO.Request.ReportsRequest;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Reports.Service.impl.ReportsService;
import org.tracker.ubus.ubus.Components.Reports.Service.interfece.IReportsService;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.ReportsResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportsController {

    private final IReportsService reportsService;

    /**
     * Gets the busiest bus route based on the provided time
     * HTTP Method: GET
     * Endpoint: /Reports/busiest-route
     *
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<ReportsResponce> containing the busiest bus routes (String key,
     *                               double value,
     *                               String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/busiest-route")
    public List<ReportsResponse> getBusiestRoute(@RequestBody ReportsRequest reportsRequest) {
            return this.reportsService.busiestRoute(reportsRequest.timeFrame());
    }

    /**
     * Gets the busiest campus based on the provided time
     * HTTP Method: GET
     * Endpoint: /Reports/busiest-campus
     *
     * @param reportsRequest DTO containing String that contains the timeFrame
     *      *     DAY,
     *      *     WEEK,
     *      *     MONTH,
     *      *     YEAR,
     *      *     ALL_TIME
     * @return List<ReportsResponse> containing the busiest campus (String key,
     *                               double value,
     *                               String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/busiest-campus")
    public List<ReportsResponse> getBusiestCampus(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.busiestCampuses(reportsRequest.timeFrame());
    }

    /**
     * Gets the average delay based on the provided time
     * HTTP Method: GET
     * Endpoint: /Reports/average-delay
     *
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return ReportsResponse containing the average delay (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/average-delay")
    public ReportsResponse getAverageDelay(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.averageDelay(reportsRequest.timeFrame());
    }

    /**
     * Gets the peak travel hours based on the provided time
     * HTTP Method: GET
     * Endpoint: /Reports/peak-travel-hours
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<ReportsResponse> containing the peak travel hours (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/peak-travel-hours")
    public List<ReportsResponse> getPeakTravelHours   (@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.peakTravelTimes(reportsRequest.timeFrame());
    }

    /**
     * Gets the number of delayed trips based on the provided time
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return ReportsResponse containing the number of delayed trips (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-delayed-trips")
    public ReportsResponse getNumDelayedTrips(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.numDelayedTrips(reportsRequest.timeFrame());
    }

    /**
     * Gets the number of trips based on the provided time
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return ReportsResponse containing the number of trips (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-trips")
    public ReportsResponse getNumTrips(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.numTrips(reportsRequest.timeFrame());
    }

    /**
     * Gets the number of cancelled trips based on the provided time
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return ReportsResponse containing the number of cancelled trips (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-cancelled-trips")
    public ReportsResponse getNumCancelledTrips(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.numCancelledTrips(reportsRequest.timeFrame());
    }

    /**
     * Gets the number of trips on time based on the provided time
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return ReportsResponse containing the number of trips on time (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-trips-on-time")
    public ReportsResponse getNumTripsOnTime(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.numTripsOnTime(reportsRequest.timeFrame());
    }

    /**
     * Gets the number of users based on the provided time
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return ReportsResponse containing the number of users (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-users")
    public ReportsResponse getNumUsers(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.numUsers(reportsRequest.timeFrame());
    }

    /**
     * Gets the number of buses based on the provided time
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return ReportsResponse containing the number of buses (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-buses")
    public ReportsResponse getNumBuses(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.numBuses(reportsRequest.timeFrame());
    }

    /**
     * Gets the list of bus utilizations based on the provided time
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return ReportsResponse containing the bus utilization (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/bus-utilization")
    public List<ReportsResponse> getBusUtilization(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.busUtilization(reportsRequest.timeFrame());
    }

    /**
     * Gets the List of average bus capacity based on the provided time
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return ReportsResponse containing the average bus capacity (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/avg-bus-capacity")
    public ReportsResponse getAvgBusCapacity(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.averageBusCapacity(reportsRequest.timeFrame());
    }

    /**
     * Gets the List of average trip lengths based on the provided time for each driver
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<ReportsResponse> containing the average trip lengths (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/avg-trip-length-by-driver")
    public List<ReportsResponse> getAvgTripLengthByDriver(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.averageTripLengthByDriver(reportsRequest.timeFrame());
    }

    /**
     * Gets the List of average trip lengths based on the provided time for each stop pair
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<ReportsResponse> containing the average trip lengths (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/avg-trip-length-by-stops")
    public List<ReportsResponse> getAvgTripLengthByStops(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.averageTripLengthByStops(reportsRequest.timeFrame());
    }

    /**
     * Gets the List of average trip lengths based on the provided time for each user
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<ReportsResponse> containing the average trip lengths (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/avg-trip-length-by-users")
    public List<ReportsResponse> getAvgTripLengthByUsers(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.averageTripLengthByUsers(reportsRequest.timeFrame());
    }

    /**
     * Gets the List of average delays based on the provided time for each driver
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<ReportsResponse> containing the average delays (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/avg-delay-by-driver")
    public List<ReportsResponse> getAvgDelayByDriver(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.averageDelayByDriver(reportsRequest.timeFrame());
    }

    /**
     * Gets the List of average delays based on the provided time for each Route
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<ReportsResponse> containing the average delays (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/avg-delay-by-route")
    public List<ReportsResponse> getAvgDelayByRoute(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.averageDelayByRoute(reportsRequest.timeFrame());
    }

    /**
        * Gets the List of number of trips based on the provided time for each Route
        * @param reportsRequest DTO containing String that contains the timeFrame
        *     DAY,
        *     WEEK,
        *     MONTH,
        *     YEAR,
        *     ALL_TIME
        * @return List<ReportsResponse> containing the number of trips (String key, double value, String valueUnits)
        */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-trips-by-route")
    public List<ReportsResponse> getNumTripsByRoute(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.numTripsByRoute(reportsRequest.timeFrame());
    }

    /**
     * Gets the List of number of trips based on the provided time for each Bus
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<ReportsResponse> containing the number of trips (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-trips-by-bus")
    public List<ReportsResponse> getNumTripsByBus(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.numTripsByBus(reportsRequest.timeFrame());
    }

    /**
     * Gets number of routes
     * @return ReportsResponse containing the number of routes (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-routes")
    public ReportsResponse getNumRoutes() {
        return this.reportsService.numRoutes();
    }

    /**
     * Gets number of drivers created before the provided time
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return ReportsResponse containing the number of drivers (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-drivers")
    public ReportsResponse getNumDrivers(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.numDrivers(reportsRequest.timeFrame());
    }

    /**
     * gets a percentage representing user growth from provided time until now
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return ReportsResponse containing the percentage growth (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/user-growth")
    public ReportsResponse getUserGrowth(@RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.userGrowth(reportsRequest.timeFrame());
    }

    /**
     * gets number of campuses
     * @return ReportsResponse containing the number of campuses (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-campuses")
    public ReportsResponse getNumCampuses() {
        return this.reportsService.numCampuses();
    }

    /**
     * gets percentage of users that have used the bus after the provided time
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return ReportsResponse containing the percentage growth (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users-that-used-bus")
    public ReportsResponse getNumCampuses( @RequestBody ReportsRequest reportsRequest) {
        return this.reportsService.percentageUsersThatHaveUsedABus(reportsRequest.timeFrame());
    }

    /**
     * Gets the average time between taps based on the provided time
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return ReportsResponse containing the number (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/ave-time-between-taps")
    public  ReportsResponse getAveTimeBetweenTaps(@RequestBody ReportsRequest reportsRequest)
    {
        return this.reportsService.averageTimeBetweenStudentsEntrance(reportsRequest.timeFrame());
    }

    /**
     * gets List of busiest buses based on the provided time
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<ReportsResponse></ReportsResponse> containing the busiest buss (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/busiest-buses")
    public List<ReportsResponse> getBusestBuses(@RequestBody ReportsRequest reportsRequest)
    {
        return this.reportsService.busiestBus(reportsRequest.timeFrame());
    }

    /**
     * gets List of drivers with the number of trips they have based on the provided time
     * @param reportsRequest DTO containing String that contains the timeFrame
     *     DAY,
     *     WEEK,
     *     MONTH,
     *     YEAR,
     *     ALL_TIME
     * @return List<ReportsResponse>containing the list (String key, double value, String valueUnits)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/num-trips-by-driver")
    public List<ReportsResponse> getNumTripsByDriver(@RequestBody ReportsRequest reportsRequest)
    {
        return this.reportsService.numTripsByDriver(reportsRequest.timeFrame());
    }
}
