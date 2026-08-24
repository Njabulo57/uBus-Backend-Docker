package org.tracker.ubus.ubus.Components.Reports.Service.impl;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Reports.Service.interfece.IReportsService;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.ReportsResponse;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Enum.TripUserStatus;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Repository.TripUserRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@org.springframework.stereotype.Service
public class ReportsService implements IReportsService {

    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private TripUserRepository tripUserRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusRepository busRepository;

    @Override
    public List<ReportsResponse> busiestRoute(ReportsTime reportsTime)
    {
        LocalDateTime dateTime = getDateTime(reportsTime);

        Map<Route, Integer> routeCountMap = new HashMap<Route,Integer>();
        int highestNumTrips = 0;
        List<ReportsResponse> busiestRoutes = new ArrayList<ReportsResponse>();

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
                busiestRoutes.add(ReportsResponse.builder().key("Route: " + entry.getKey()).value(entry.getValue()).valueUnits("trips").build());
            }
        }
        return busiestRoutes;
    }

    @Override
    public ReportsResponse numRoutes() {
       return ReportsResponse.builder().key("Number of Routes").value(Route.values().length).valueUnits("routes").build();
    }

    @Override
    public List<ReportsResponse> averageDelayByRoute(ReportsTime reportsTime) {
//        LocalDateTime dateTime = getDateTime(reportsTime);
//        List<Trip> trips;
//        Map<Route, Double> totalDelaysInSecs = new HashMap<>();
//        Map<Route, Integer> tripCountMap = new HashMap<>();
//        if(dateTime == null)
//        {
//            trips = tripRepository.findAll();
//        }
//        else {
//            trips = tripRepository.findByCreatedAtAfter(dateTime);
//        }
//        for(Trip trip: trips)
//        {
//            Schedule schedule = trip.getScheduleLegBusAssignment()
//                    .getScheduleLeg();
//            if(schedule == null || trip.getDepartureTime() == null)
//                continue;
//            LocalDateTime scheduledDeparture = LocalDateTime.of(schedule.getServiceDate(), schedule.getDepartureTime());
//            Duration duration = Duration.between(scheduledDeparture, trip.getDepartureTime());
//            totalDelaysInSecs.merge(trip.getRoute(), (double) duration.getSeconds(), Double::sum);
//            tripCountMap.merge(trip.getRoute(), 1, Integer::sum);
//        }
//        List<ReportsResponse> averageDelays = new ArrayList<>();
//        for(Map.Entry<Route, Double> entry : totalDelaysInSecs.entrySet())
//        {
//            Route route = entry.getKey();
//            double totalDelay = entry.getValue();
//            int tripCount = tripCountMap.get(route);
//            double averageDelay = totalDelay / tripCount;
//            averageDelays.add(ReportsResponse.builder().key("Route: " + route).value(averageDelay).valueUnits("seconds").build());
//        }
//        return averageDelays;

        return Collections.emptyList();
    }
    @Override
    public List<ReportsResponse> busiestCampuses(ReportsTime reportsTime) {
//        LocalDateTime dateTime = getDateTime(reportsTime);
//
//        List<TripUser> enterTripUsers;
//        List<TripUser> exitTripUsers;
//        if(dateTime == null)
//        {
//            enterTripUsers = tripUserRepository.findAllByIsFirstTrip(true);
//            exitTripUsers = tripUserRepository.findAllByStatus(TripUserStatus.EXITED);
//        }
//        else
//        {
//            enterTripUsers = tripUserRepository.findAllByCreatedAtAfterAndIsFirstTrip(dateTime, true);
//            exitTripUsers = tripUserRepository.findAllByCreatedAtAfterAndStatus(dateTime, TripUserStatus.EXITED);
//        }
//
//        Map<Destination, Integer> campusEnterCountMap = new HashMap<Destination,Integer>();
//        Map<Destination, Integer> campusExitCountMap = new HashMap<Destination,Integer>();
//
//        int highestCombination = 0;
//
//        for(TripUser tripUser : enterTripUsers)
//        {
//            Destination destination = tripUser.getTrip().getSchedule().getFromDestination();
//            campusEnterCountMap.merge(destination, 1, Integer::sum);
//        }
//
//        for(TripUser tripUser : exitTripUsers)
//        {
//            Destination destination = tripUser.getTrip().getSchedule().getToDestination();
//            campusExitCountMap.merge(destination, 1, Integer::sum);
//        }
//
//
//        for(Destination destination : Destination.values())
//        {
//            int enterCount = campusEnterCountMap.getOrDefault(destination, 0);
//            int exitCount = campusExitCountMap.getOrDefault(destination, 0);
//            int combination = enterCount + exitCount;
//
//            if(combination > highestCombination)
//            {
//                highestCombination = combination;
//            }
//        }
//
//        List<ReportsResponse> busiestCampuses = new ArrayList<>();
//        for(Destination destination : Destination.values())
//        {
//            int enterCount = campusEnterCountMap.getOrDefault(destination, 0);
//            int exitCount = campusExitCountMap.getOrDefault(destination, 0);
//            int combination = enterCount + exitCount;
//
//            if(combination == highestCombination)
//            {
//                busiestCampuses.add(ReportsResponse.builder().key("Campus: " + destination).value(combination).valueUnits("entrances and exits").build());
//            }
//        }
//
//        return busiestCampuses;

        return Collections.emptyList();
    }

    @Override
    public ReportsResponse numCampuses() {
        return ReportsResponse.builder().key("Number of Campuses").value(Destination.values().length).valueUnits("campuses").build();
    }

    @Override
    public List<ReportsResponse> averageTripLengthByUsers(ReportsTime reportsTime) {
//       LocalDateTime dateTime = getDateTime(reportsTime);
//        List<TripUser> tripUsers;
//        List<ReportsResponse> averageTripLengths = new ArrayList<>();
//        if(dateTime == null)
//        {
//            tripUsers = tripUserRepository.findAll();
//        }
//        else {
//            tripUsers = tripUserRepository.findAllByCreatedAtAfter(dateTime);
//        }
//        Map<TripUser, Double> totalTripLengthsInSecs = new HashMap<>();
//        Map<TripUser, Integer> tripCountMap = new HashMap<>();
//
//        for (TripUser tripUser : tripUsers) {
//            Trip trip = tripUser.getTrip();
//            if(trip.getDepartureTime() == null || trip.getActualArrivalTime() == null)
//                continue;
//            Duration duration = Duration.between(trip.getDepartureTime(), trip.getActualArrivalTime());
//            double tripLengthInSecs = duration.getSeconds();
//            totalTripLengthsInSecs.merge(tripUser, tripLengthInSecs, Double::sum);
//            tripCountMap.merge(tripUser, 1, Integer::sum);
//        }
//        for (Map.Entry<TripUser, Double> entry : totalTripLengthsInSecs.entrySet()) {
//            averageTripLengths.add(
//                    ReportsResponse.builder()
//                            .key("User: " + entry.getKey().getUser().getId())
//                            .value(entry.getValue() / tripCountMap.get(entry.getKey()))
//                            .valueUnits("seconds")
//                            .build()
//            );
//        }
//        return averageTripLengths;
        return Collections.emptyList();
    }

    @Override
    public ReportsResponse numUsers(ReportsTime reportsTime) {
       LocalDateTime dateTime = getDateTime(reportsTime);
        int numUsers = 0;
        if(dateTime == null)
        {
            numUsers = (int)userRepository.count();
        }
        else {
            numUsers =userRepository.countByCreatedAtBefore(dateTime);
        }
        return ReportsResponse.builder().key("Number of Users").value(numUsers).valueUnits("users").build();
    }

    @Override
    public ReportsResponse userGrowth(ReportsTime reportsTime) {
        double totalUsers = this.numUsers(ReportsTime.ALL_TIME).value();
        double periodUsers = this.numUsers(reportsTime).value();
        if (periodUsers == 0) {
            return ReportsResponse.builder()
                    .key("User Growth")
                    .value(0)
                    .valueUnits("%")
                    .build();
        }

        double growth = (periodUsers - totalUsers) / totalUsers * 100;
        return ReportsResponse.builder()
                .key("User Growth")
                .value(growth)
                .valueUnits("%")
                .build();
    }

    @Override
    public ReportsResponse percentageUsersThatHaveUsedABus(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        int numUsers = (int) userRepository.count();
        int numBusUsers = 0;
        if(dateTime == null)
        {
            numBusUsers = tripUserRepository.countUniqueUserIds();
        }
        else {
            numBusUsers = tripUserRepository.countUniqueUserIdsByCreatedAtAfter(dateTime);
        }
        double percentage = numUsers > 0 ? (numBusUsers * 100.0) / numUsers : 0;

        return ReportsResponse.builder().key("Percentage of Users that have used a bus in specified time").value(percentage).valueUnits("%").build();
    }

    @Override
    public ReportsResponse averageTimeBetweenStudentsEntrance(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        List<Trip> trips;
        int numEntrances = 0;
        double totalTime = 0;
        if(dateTime == null)
        {
            trips = tripRepository.findAll();
        }
        else {
            trips = tripRepository.findByCreatedAtAfter(dateTime);
        }

        for(Trip trip: trips)
        {
            List<TripUser> users = tripUserRepository.findByTrip(trip);
            numEntrances += users.size();
            if(users.size() < 2)
                continue;
            TripUser firstTripUser = users.getFirst();
            TripUser lastTripUser = users.getLast();
            Duration duration = Duration.between(firstTripUser.getCreatedAt(), lastTripUser.getCreatedAt());
            totalTime += duration.getSeconds();
        }
        return ReportsResponse.builder().key("Average Time Between Students Entrance").value(totalTime/numEntrances).valueUnits("seconds").build();
    }



    @Override
    public ReportsResponse numBuses(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        int numBuses = 0;
        if(dateTime == null)
        {
            numBuses = (int) busRepository.count();
        }
        else {
            numBuses = busRepository.countByCreatedAtBefore(dateTime);
        }
        return ReportsResponse.builder().key("Number of Buses").value(numBuses).valueUnits("buses").build();
    }

    @Override
    public List<ReportsResponse> busiestBus(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        List<Trip> trips;
        if(dateTime == null)
        {
            trips = tripRepository.findAll();
        }
        else {
            trips = tripRepository.findByCreatedAtAfter(dateTime);
        }

        Map<String, Integer> busCountMap = new HashMap<>();
        int highestNumTrips = 0;
        List<ReportsResponse> busiestBuses = new ArrayList<>();

        for(Trip trip : trips)
        {
            String busId = trip.getBusAssignment().getBus().getId().toString();
            busCountMap.merge(busId, 1, Integer::sum);
        }

        for(Map.Entry<String, Integer> entry : busCountMap.entrySet())
        {
            if(entry.getValue() > highestNumTrips)
            {
                highestNumTrips = entry.getValue();
            }
        }
        for(Map.Entry<String, Integer> entry : busCountMap.entrySet())
        {
            if(entry.getValue() == highestNumTrips)
            {
                busiestBuses.add(ReportsResponse.builder().key("Bus ID: " + entry.getKey()).value(entry.getValue()).valueUnits("trips").build());
            }
        }
      return busiestBuses;
    }

    @Override
    public List<ReportsResponse> busUtilization(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        List<Trip> trips;
        Map<Bus, Integer> busCountMap = new HashMap<>();

        if(dateTime == null)
        {
            trips = tripRepository.findAll();
        }
        else {
            trips = tripRepository.findByCreatedAtAfter(dateTime);
        }
        int totalTrips = trips.size();

        for(Trip trip : trips)
        {
            Bus bus = trip.getBusAssignment().getBus();
            busCountMap.merge(bus, 1, Integer::sum);
        }
        List<ReportsResponse> busUtilization = new ArrayList<>();
        for(Map.Entry<Bus, Integer> entry : busCountMap.entrySet())
        {
            busUtilization.add(ReportsResponse.builder().key("Bus ID: " + entry.getKey().getId()).value(entry.getValue() * 100.0 / totalTrips).valueUnits("percentage").build());
        }
        return busUtilization;

    }

    @Override
    public ReportsResponse averageBusCapacity(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);

        List<Trip> trips;
        int totalCapacity = 0;
        int totalTrips = 0;
        if (dateTime == null) {
            trips = tripRepository.findAll();
        } else {
            trips = tripRepository.findByCreatedAtAfter(dateTime);
        }
        for (Trip trip : trips) {
            if(trip.getBusAssignment() != null && trip.getBusAssignment().getBus() != null)
            {
                totalCapacity += trip.getBusAssignment().getBus().getCapacity();
                totalTrips++;
            }
        }
        return ReportsResponse.builder().key("Average Bus Capacity").value((double) totalCapacity / totalTrips).valueUnits("seats").build();
    }

    @Override
    public List<ReportsResponse> numTripsByBus(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        List<Trip> trips;
        if(dateTime == null)
        {
            trips = tripRepository.findAll();
        }
        else {
            trips = tripRepository.findByCreatedAtAfter(dateTime);
        }
        Map<Bus, Integer> tripCountMap = new HashMap<>();
        for(Trip trip : trips)
        {
            tripCountMap.merge(trip.getBusAssignment().getBus(), 1, Integer::sum);
        }
        List<ReportsResponse> busCounts = new ArrayList<>();
        for(Map.Entry<Bus, Integer> entry : tripCountMap.entrySet())
        {
            busCounts.add(ReportsResponse.builder().key("Bus ID: " + entry.getKey().getId()).value(entry.getValue()).valueUnits("trips").build());
        }
        return busCounts;
    }

    @Override
    public ReportsResponse numDrivers(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        int numDrivers = 0;
        if(dateTime == null)
        {
            numDrivers = userRepository.countByRole(UserRole.DRIVER);
        }
        else {
            numDrivers = userRepository.countByRoleAndCreatedAtAfter(UserRole.DRIVER, dateTime);
        }
        return ReportsResponse.builder().key("Number of Drivers").value(numDrivers).valueUnits("drivers").build();
    }

    @Override
    public List<ReportsResponse> averageTripLengthByDriver(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        List<Trip> trips;
        if(dateTime == null)
        {
            trips = tripRepository.findAll();
        }
        else {
            trips = tripRepository.findByCreatedAtAfter(dateTime);
        }
        Map<User, Double> totalTripLengthsInSecs = new HashMap<>();
        Map<User, Integer> tripCountMap = new HashMap<>();

        for(Trip trip: trips)
        {
            if(trip.getDepartureTime() == null || trip.getActualArrivalTime() == null)
                continue;
            totalTripLengthsInSecs.merge(trip.getBusAssignment().getDriver(), (double) Duration.between(trip.getDepartureTime(), trip.getActualArrivalTime()).getSeconds(), Double::sum);
            tripCountMap.merge(trip.getBusAssignment().getDriver(), 1, Integer::sum);
        }

        return getReportsResponses(totalTripLengthsInSecs, tripCountMap);
    }

    @Override
    public List<ReportsResponse> averageDelayByDriver(ReportsTime reportsTime) {
//        LocalDateTime dateTime = getDateTime(reportsTime);
//        List<Trip> trips;
//        if(dateTime == null)
//        {
//            trips = tripRepository.findAll();
//        }
//        else {
//            trips = tripRepository.findByCreatedAtAfter(dateTime);
//        }
//        Map<User, Double> totalDelaysInSecs = new HashMap<>();
//        Map<User, Integer> tripCountMap = new HashMap<>();
//
//        for(Trip trip: trips)
//        {
//            Schedule schedule = trip.getSchedule();
//            if(schedule == null || trip.getDepartureTime() == null)
//                continue;
//            LocalDateTime scheduledDeparture = LocalDateTime.of(schedule.getServiceDate(), schedule.getDepartureTime());
//            Duration duration = Duration.between(scheduledDeparture, trip.getDepartureTime());
//            totalDelaysInSecs.merge(trip.getBusAssignment().getDriver(), (double) duration.getSeconds(), Double::sum);
//            tripCountMap.merge(trip.getBusAssignment().getDriver(), 1, Integer::sum);
//        }


//        return getReportsResponses(totalDelaysInSecs, tripCountMap);

        return Collections.emptyList();
    }

    @NonNull
    private List<ReportsResponse> getReportsResponses(Map<User, Double> totalDelaysInSecs, Map<User, Integer> tripCountMap) {
        List<ReportsResponse> averageDelays = new ArrayList<>();
        for (Map.Entry<User, Integer> entry : tripCountMap.entrySet()) {
            User driver = entry.getKey();
            int tripCount = entry.getValue();
            double totalDelay = totalDelaysInSecs.getOrDefault(driver, 0.0);
            double averageDelay = totalDelay / tripCount;
            averageDelays.add(ReportsResponse.builder().key("Driver: " + driver.getId()).value(averageDelay).valueUnits("seconds").build());
        }

        return averageDelays;
    }

    @Override
    public List<ReportsResponse> numTripsByDriver(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        List<Trip> trips;
        if(dateTime == null)
        {
            trips = tripRepository.findAll();
        }
        else {
            trips = tripRepository.findByCreatedAtAfter(dateTime);
        }
        Map<User, Integer> tripCountMap = new HashMap<>();
        for(Trip trip : trips)
        {
            tripCountMap.merge(trip.getBusAssignment().getDriver(), 1, Integer::sum);
        }
        List<ReportsResponse> driverCounts = new ArrayList<>();
        for(Map.Entry<User, Integer> entry : tripCountMap.entrySet())
        {
            driverCounts.add(ReportsResponse.builder().key("Driver: " + entry.getKey().getId()).value(entry.getValue()).valueUnits("trips").build());
        }
        return driverCounts;
    }

    public List<ReportsResponse> peakTravelTimes(ReportsTime reportsTime)
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

        List<ReportsResponse> peakHours = new ArrayList<>();
        for(Map.Entry<Integer, Integer> entry : numUsersPerHour.entrySet())
        {
            if(entry.getValue() == highestNumUsers)
            {
                peakHours.add(ReportsResponse.builder().key("Hour: " + entry.getKey()).value(entry.getValue()).valueUnits("users").build());
            }
        }

        return peakHours;
    }

    @Override
    public ReportsResponse numDelayedTrips(ReportsTime reportsTime) {
//        LocalDateTime dateTime = getDateTime(reportsTime);
//        List<Trip> trips = tripRepository.findAllWithScheduleFetched(dateTime);
//
//        int numDelayed = 0;
//
//        for (Trip trip : trips) {
//            Schedule schedule = trip.getSchedule();
//            if(schedule == null || trip.getDepartureTime() == null)
//                continue;
//            LocalDateTime scheduledDeparture = LocalDateTime.of(schedule.getServiceDate(), schedule.getDepartureTime());
//            Duration duration = Duration.between(scheduledDeparture, trip.getDepartureTime());
//            if (duration.getSeconds() > (60*5)) {
//                numDelayed++;
//            }
//        }
//        return ReportsResponse.builder().key("Delayed Trips").value(numDelayed).valueUnits("trips").build();

        return ReportsResponse.builder().key("Delayed Trips").value(0).valueUnits("trips").build();
    }

    @Override
    public ReportsResponse numCancelledTrips(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        return ReportsResponse.builder().key("Cancelled Trips").value(tripRepository.countAllWithScheduleFetchedAndStatus(dateTime, TripStatus.CANCELLED)).valueUnits("trips").build();
    }

    @Override
    public ReportsResponse numTrips(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        return ReportsResponse.builder().key("Total Trips").value(tripRepository.countAllWithScheduleFetched(dateTime)).valueUnits("trips").build();
    }

    @Override
    public ReportsResponse numTripsOnTime(ReportsTime reportsTime) {
//        LocalDateTime dateTime = getDateTime(reportsTime);
//        List<Trip> trips = tripRepository.findAllWithScheduleFetched(dateTime);
//
//        int numOntime = 0;
//
//        for (Trip trip : trips) {
//            Schedule schedule = trip.getSchedule();
//            if(schedule == null || trip.getDepartureTime() == null)
//                continue;
//            LocalDateTime scheduledDeparture = LocalDateTime.of(schedule.getServiceDate(), schedule.getDepartureTime());
//            Duration duration = Duration.between(scheduledDeparture, trip.getDepartureTime());
//            if (duration.getSeconds() < (60*5)) {
//                numOntime++;
//            }
//        }
//        return ReportsResponse.builder().key("On Time").value(numOntime).valueUnits("trips").build();

        return null;
    }


    @Override
    public List<ReportsResponse> numTripsByRoute(ReportsTime reportsTime) {
//        LocalDateTime dateTime = getDateTime(reportsTime);
//        List<Trip> trips = tripRepository.findAllWithScheduleFetched(dateTime);
//
//        List<ReportsResponse> routeCounts = new ArrayList<>();
//        Map<Route, Integer> routeCountMap = new HashMap<>();
//        for (Trip trip : trips) {
//            if(trip.getRoute() != null) {
//                routeCountMap.merge(trip.getRoute(), 1, Integer::sum);
//            }
//        }
//        for (Map.Entry<Route, Integer> entry : routeCountMap.entrySet()) {
//            routeCounts.add(ReportsResponse.builder().key(entry.getKey().getLabel()).value(entry.getValue()).valueUnits("trips").build());
//        }
//        return routeCounts;

        return Collections.emptyList();
    }

    @Override
    public List<ReportsResponse> averageTripLengthByStops(ReportsTime reportsTime) {
//        LocalDateTime dateTime = getDateTime(reportsTime);
//        List<Trip> trips = tripRepository.findAllWithScheduleFetched(dateTime);
//
//        Map<String, Double> totalTripLengthsInSecs = new HashMap<>();
//        Map<String, Integer> routeCountMap = new HashMap<>();
//
//        for (Trip trip : trips) {
//            if(trip.getDepartureTime() == null || trip.getActualArrivalTime() == null)
//                continue;
//            Duration duration = Duration.between(trip.getDepartureTime(), trip.getActualArrivalTime());
//            double tripLengthInSecs = duration.getSeconds();
//
//            Destination from = trip.getSchedule().getFromDestination();
//            Destination to = trip.getSchedule().getToDestination();
//
//           //creating key for stop pair
//            String routeKey;
//            if (from.getLabel().compareTo(to.getLabel()) < 0) {
//                routeKey = from.getLabel() + " - " + to.getLabel();
//            } else {
//                routeKey = to.getLabel() + " - " + from.getLabel();
//            }
//
//            totalTripLengthsInSecs.merge(routeKey, tripLengthInSecs, Double::sum);
//            routeCountMap.merge(routeKey, 1, Integer::sum);
//        }
//
//        List<ReportsResponse> averageTripLengths = new ArrayList<>();
//        for (Map.Entry<String, Double> entry : totalTripLengthsInSecs.entrySet()) {
//            String routeKey = entry.getKey();
//            double totalLength = entry.getValue();
//            int count = routeCountMap.get(routeKey);
//            double averageLength = totalLength / count;
//
//            averageTripLengths.add(
//                    ReportsResponse.builder()
//                            .key(routeKey)
//                            .value(averageLength)
//                            .valueUnits("seconds")
//                            .build()
//            );
//        }

        return Collections.emptyList();
    }

    @Override
    public ReportsResponse averageDelay(ReportsTime reportsTime) {
//        LocalDateTime dateTime = getDateTime(reportsTime);
//        List<Trip> trips = tripRepository.findAllWithScheduleFetched(dateTime);
//
//        float totalDelaySec = 0;
//        int count = 0;
//
//        for (Trip trip : trips) {
//            Schedule schedule = trip.getSchedule();
//
//            if(schedule == null ||
//            schedule.getServiceDate() == null ||
//            schedule.getDepartureTime() == null ||
//            trip.getDepartureTime() == null)
//            {
//                continue;
//            }
//            LocalDateTime scheduledDeparture = LocalDateTime.of(schedule.getServiceDate(), schedule.getDepartureTime());
//            Duration duration = Duration.between(scheduledDeparture, trip.getDepartureTime());
//
//            totalDelaySec += duration.getSeconds();
//            count++;
//            }
//
//        if(count == 0)
//            return new ReportsResponse("Average Delay", 0, "seconds");
//        else
//        {
//            return new ReportsResponse("Average Delay", totalDelaySec/count, "seconds");
//        }

        return null;
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
