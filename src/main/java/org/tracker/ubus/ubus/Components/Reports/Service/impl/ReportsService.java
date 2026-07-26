package org.tracker.ubus.ubus.Components.Reports.Service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Reports.Service.interfece.IReportsService;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Enum.TripUserStatus;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Repository.TripUserRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class ReportsService implements IReportsService {

    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private TripUserRepository tripUserRepository;

    public List<String> busiestRoute(ReportsTime reportsTime)
    {
        LocalDateTime dateTime = getDateTime(reportsTime);

        Map<Route, Integer> routeCountMap = new HashMap<Route,Integer>();
        int highestNumTrips = 0;
        List<String> busiestRoutes = new ArrayList<String>();

        List<Trip> trips;
        if(dateTime == null)
        {
            trips = tripRepository.findAll();
        }
        else{
            trips = tripRepository.findByCreatedAtAfter(dateTime);
        }

        for(Trip trip : trips)
        {
            Route route = trip.getRoute();
            routeCountMap.merge(route, 1, Integer::sum);
        }


        for(Map.Entry<Route, Integer> entry : routeCountMap.entrySet())
        {
            if(entry.getValue() > highestNumTrips)
            {
                highestNumTrips = entry.getValue();
            }
        }
        for(Map.Entry<Route, Integer> entry : routeCountMap.entrySet())
        {
            if(entry.getValue() == highestNumTrips)
            {
                busiestRoutes.add(entry.getKey().toString() + ": " +entry.getValue());
            }
        }
        return busiestRoutes;
    }

    public List<String> busiestCampuses(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);

        List<TripUser> enterTripUsers;
        List<TripUser> exitTripUsers;
        if(dateTime == null)
        {
            enterTripUsers = tripUserRepository.findAllByIsFirstTrip(true);
            exitTripUsers = tripUserRepository.findAllByStatus(TripUserStatus.EXITED);
        }
        else
        {
            enterTripUsers = tripUserRepository.findAllByCreatedAtAfterAndIsFirstTrip(dateTime, true);
            exitTripUsers = tripUserRepository.findAllByCreatedAtAfterAndStatus(dateTime, TripUserStatus.EXITED);
        }

        Map<Destination, Integer> campusEnterCountMap = new HashMap<Destination,Integer>();
        Map<Destination, Integer> campusExitCountMap = new HashMap<Destination,Integer>();

        int highestCombination = 0;

        for(TripUser tripUser : enterTripUsers)
        {
            Destination destination = tripUser.getTrip().getSchedule().getFromDestination();
            campusEnterCountMap.merge(destination, 1, Integer::sum);
        }

        for(TripUser tripUser : exitTripUsers)
        {
            Destination destination = tripUser.getTrip().getSchedule().getToDestination();
            campusExitCountMap.merge(destination, 1, Integer::sum);
        }


        for(Destination destination : Destination.values())
        {
            int enterCount = campusEnterCountMap.getOrDefault(destination, 0);
            int exitCount = campusExitCountMap.getOrDefault(destination, 0);
            int combination = enterCount + exitCount;

            if(combination > highestCombination)
            {
                highestCombination = combination;
            }
        }

        List<String> busiestCampuses = new ArrayList<>();
        for(Destination destination : Destination.values())
        {
            int enterCount = campusEnterCountMap.getOrDefault(destination, 0);
            int exitCount = campusExitCountMap.getOrDefault(destination, 0);
            int combination = enterCount + exitCount;

            if(combination == highestCombination)
            {
                busiestCampuses.add(destination.toString() + ": Entrances: " + enterCount + " Exits: " + exitCount);
            }
        }

        return busiestCampuses;
    }

    public List<String> peakTravelTimes(ReportsTime reportsTime)
    {
        LocalDateTime dateTime = getDateTime(reportsTime);
        List<TripUser> tripUsers;
        if(dateTime == null)
        {
            tripUsers = tripUserRepository.findAll();
        }
        else {
            tripUsers = tripUserRepository.findAllByCreatedAtAfter(dateTime);
        }

        Map<Integer, Integer> numUsersPerHour = new HashMap<>();
        int startHour = 6;
        int endHour = 23;

        int highestNumUsers = 0;

        for(int index = startHour; index < endHour; index++)
        {
            numUsersPerHour.put(index, 0);
        }

        for(TripUser tripUser : tripUsers)
        {
            int hour = tripUser.getCreatedAt().getHour();
            if(hour >= startHour && hour < endHour) {
                numUsersPerHour.merge(hour, 1, Integer::sum);
            }
        }

        for(Map.Entry<Integer, Integer> entry : numUsersPerHour.entrySet())
        {
            if(entry.getValue() > highestNumUsers)
            {
                highestNumUsers = entry.getValue();
            }
        }

        List<String> peakHours = new ArrayList<>();
        for(Map.Entry<Integer, Integer> entry : numUsersPerHour.entrySet())
        {
            if(entry.getValue() == highestNumUsers)
            {
                peakHours.add("Hour: " + entry.getKey() + ", Users: " + entry.getValue());
            }
        }

        return peakHours;
    }

    public String averageDelay(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        List<Trip> trips = tripRepository.findAllWithScheduleFetched(dateTime);

        float totalDelaySec = 0;
        int count = 0;

        for (Trip trip : trips) {
            Schedule schedule = trip.getSchedule();
            LocalDateTime scheduledDeparture = LocalDateTime.of(schedule.getServiceDate(), schedule.getDepartureTime());
            Duration duration = Duration.between(scheduledDeparture, trip.getDepartureTime());

            totalDelaySec += duration.getSeconds();
            count++;
            }

        if(count == 0)
            return "0";
        else
        {
            return String.format("%.2f", totalDelaySec/count);
        }
    }

    private LocalDateTime getDateTime(ReportsTime reportsTime) {
        LocalDateTime dateTime = LocalDateTime.now();
        switch (reportsTime) {
            case DAY: {
                dateTime = dateTime.minusDays(1);
                break;
            }
            case MONTH: {
                dateTime = dateTime.minusMonths(1);
                break;
            }
            case YEAR: {
                dateTime = dateTime.minusYears(1);
                break;
            }
            case ALL_TIME: {
                dateTime = null;
                break;
            }
        }
        return dateTime;
    }
}
