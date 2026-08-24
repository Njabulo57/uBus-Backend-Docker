package org.tracker.ubus.ubus.Components.Reports.Service.interfece;

import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.ReportsResponse;

import java.util.List;

public interface IReportsService {

    //Trips

    public ReportsResponse averageDelay(ReportsTime reportsTime);
    public List<ReportsResponse> peakTravelTimes(ReportsTime reportsTime);
    public ReportsResponse numDelayedTrips(ReportsTime reportsTime);
    public ReportsResponse numCancelledTrips(ReportsTime reportsTime);
    public ReportsResponse numTrips(ReportsTime reportsTime);
    public ReportsResponse numTripsOnTime(ReportsTime reportsTime);

    //Routes
    public List<ReportsResponse> numTripsByRoute(ReportsTime reportsTime);
    public List<ReportsResponse> busiestRoute(ReportsTime reportsTime);
    public ReportsResponse numRoutes();
    public List<ReportsResponse> averageDelayByRoute(ReportsTime reportsTime);

    //Campuses
    public List<ReportsResponse> busiestCampuses(ReportsTime reportsTime);
    public ReportsResponse numCampuses();
    public List<ReportsResponse> averageTripLengthByStops(ReportsTime reportsTime);

    //Users
    public List<ReportsResponse> averageTripLengthByUsers(ReportsTime reportsTime);
    public ReportsResponse numUsers(ReportsTime reportsTime);
    public ReportsResponse userGrowth(ReportsTime reportsTime);
    public ReportsResponse percentageUsersThatHaveUsedABus(ReportsTime reportsTime);
    public ReportsResponse averageTimeBetweenStudentsEntrance(ReportsTime reportsTime);

    //Buses
    public ReportsResponse numBuses(ReportsTime reportsTime);
    public List<ReportsResponse> busiestBus(ReportsTime reportsTime);
    public List<ReportsResponse> busUtilization(ReportsTime reportsTime);
    public ReportsResponse averageBusCapacity(ReportsTime reportsTime);
    public List<ReportsResponse> numTripsByBus(ReportsTime reportsTime);

    //Drivers
    public ReportsResponse numDrivers(ReportsTime reportsTime);
    public List<ReportsResponse> averageTripLengthByDriver(ReportsTime reportsTime);
    public List<ReportsResponse> averageDelayByDriver(ReportsTime reportsTime);
    public List<ReportsResponse> numTripsByDriver(ReportsTime reportsTime);

}