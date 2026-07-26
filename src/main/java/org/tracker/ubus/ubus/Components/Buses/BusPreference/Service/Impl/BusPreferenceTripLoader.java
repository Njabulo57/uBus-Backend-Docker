package org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Impl;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Internal.ClosestTripInfo;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.UserPreferenceNearestBus;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Handlers.DefaultRouteServiceCacheHandler;
import org.tracker.ubus.ubus.Components.Trips.Trip.CacheManager.TripCacheManager;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Util.EtaCalculator;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


@Slf4j
@Component
@RequiredArgsConstructor
public class BusPreferenceTripLoader {

    private final Cache<UUID, Trip> tripCache;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm a");
    private final DefaultRouteServiceCacheHandler defaultRouteServiceCacheHandler;
    private final Map<UUID, ConcurrentLinkedDeque<DriverCurrentLocationMessage>> busQueues;
    private final TripCacheManager manager;

    public Collection<Trip> getCurrentTrips() {
        return manager.getAll();
    }


    public Map<User, List<UserPreferenceNearestBus>> findNearestBusForUserPreference(Collection<BusPreference> busPreferences) {

        var allNearestTripsMap = this.getNearestTripsToTheirDestination();

        var userNearestBuses = new HashMap<User, List<UserPreferenceNearestBus>>();

        for(var preference : busPreferences ) {

            var user = preference.getUser();
            var route = preference.getRoute();
            var userDestinations = preference.getBusUserPrefDestinations();

            var userBuses = userNearestBuses.computeIfAbsent(user,
                    userKey -> new ArrayList<>());

            for(var userDest: userDestinations) {
                var from = userDest.getFromDestination();
                var to = userDest.getToDestination();

                var nearestBus = this.getNearestBusForRouteAndDestination(allNearestTripsMap,
                        route, to, from);

                if(nearestBus != null)
                    userBuses.add(nearestBus);

            }

        }
        return userNearestBuses;
    }


    public UserPreferenceNearestBus getNearestBusForRouteAndDestination(Map<Route, Map<Destination, ClosestTripInfo>> allNearestTripsMap,
                                                                        Route route,
                                                                        Destination userToDest, Destination userFromDest) {
        var destinationMap = allNearestTripsMap.get(route);
        if(destinationMap == null)
            return null;

        var userToDestInfo = destinationMap.get(userToDest);
        if(userToDestInfo == null)
            return null;

        // Use the OLD method to calculate stops (not the map)
        int stopsToUser = this.calculateStopsToUser(route, userToDestInfo.trip().getSchedule().getFromDestination(), userToDest);
        boolean isUserAtStop = stopsToUser == 0;

        // Use the OLD method to calculate total stops
        int totalStopsForJourney = this.calculateJourneyStopsBetweenDestinations(route, userFromDest, userToDest);

        return UserPreferenceNearestBus.builder()
                .stops(stopsToUser)
                .from(userFromDest)
                .to(userToDest)
                .isAtUserStop(isUserAtStop)
                .totalStopsForJourney(totalStopsForJourney)
                .nearestTrip(userToDestInfo)
                .build();

    }


    public int calculateStopsToUser(Route route, Destination busCurrentLocation, Destination userBoardingStop) {
        List<Destination> destinations = route.getDestinations();

        int currentIndex = destinations.indexOf(busCurrentLocation);
        int userIndex = destinations.indexOf(userBoardingStop);

        if (currentIndex == -1 || userIndex == -1) {
            log.warn("Destination not found in route: busCurrent={}, userStop={}", busCurrentLocation, userBoardingStop);
            return 0;
        }

        // Count unique stops between current location and user stop
        int stops = 0;
        Set<Destination> uniqueStops = new LinkedHashSet<>();

        if (currentIndex < userIndex) {
            // Moving forward in the route
            for (int i = currentIndex + 1; i <= userIndex; i++) {
                uniqueStops.add(destinations.get(i));
            }
        } else {
            // Moving backward (wrap around)
            for (int i = currentIndex + 1; i < destinations.size(); i++) {
                uniqueStops.add(destinations.get(i));
            }
            for (int i = 0; i <= userIndex; i++) {
                uniqueStops.add(destinations.get(i));
            }
        }

        stops = uniqueStops.size();
        log.debug("Stops from {} to {}: {}", busCurrentLocation, userBoardingStop, stops);
        return stops;
    }



    public int calculateJourneyStopsBetweenDestinations(Route route, Destination userBoardingStop, Destination userDestinationStop) {
        List<Destination> destinations = route.getDestinations();

        int startIndex = destinations.indexOf(userBoardingStop);
        int endIndex = destinations.indexOf(userDestinationStop);

        if (startIndex == -1 || endIndex == -1) {
            log.warn("Destination not found in route: start={}, end={}", userBoardingStop, userDestinationStop);
            return 0;
        }

        // Count unique stops for the journey
        Set<Destination> uniqueStops = new LinkedHashSet<>();

        if (startIndex < endIndex) {
            // Moving forward
            for (int i = startIndex + 1; i <= endIndex; i++) {
                uniqueStops.add(destinations.get(i));
            }
        } else {
            // Moving backward (wrap around)
            for (int i = startIndex + 1; i < destinations.size(); i++) {
                uniqueStops.add(destinations.get(i));
            }
            for (int i = 0; i <= endIndex; i++) {
                uniqueStops.add(destinations.get(i));
            }
        }

        int totalStops = uniqueStops.size();
        log.debug("Total stops from {} to {}: {}", userBoardingStop, userDestinationStop, totalStops);
        return totalStops;
    }


    private List<Integer> getAllIndices(List<Destination> destinations, Destination target) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < destinations.size(); i++)
            if (destinations.get(i) == target)
                indices.add(i);
        return indices;
    }


    private int countUniqueStops(List<Destination> destinations, int startIndex, int endIndex) {
        Set<Destination> uniqueDestinations = new LinkedHashSet<>();
        for(int i = startIndex + 1; i < endIndex && i < destinations.size(); i++)
            uniqueDestinations.add(destinations.get(i));
        return uniqueDestinations.size();
    }


    public Map<Route, Map<Destination, ClosestTripInfo>> getNearestTripsToTheirDestination() {
        var allTrips = this.getCurrentTrips();
        var routeDestinationMap = new HashMap<Route, Map<Destination, ClosestTripInfo>>();

        for (var trip : allTrips) {
            var route = trip.getRoute();
            var distanceToDestination = this.calculateDistance(trip);

            var speed = this.getCurrentLocationSpeed(trip);
            var kmEta = roundOff(distanceToDestination / 1000, 2);
            var strKM = kmEta + " km";

            var eta = EtaCalculator.calculateETA(distanceToDestination, speed);

            log.info("ETA is {} distance {}", eta, strKM);
            var delayStatus = EtaCalculator.containsDelay(trip.getSchedule().getArrivalTime(), eta);
            var formattedEta = eta.format(formatter);

            var bus = trip.getSchedule().getBus();

            var closestTripInfo = ClosestTripInfo.builder()
                    .eta(formattedEta)
                    .distance(strKM)
                    .distanceInKM(kmEta)
                    .busName(bus.getName())
                    .delayStatus(delayStatus)
                    .trip(trip)
                    .build();

            var currentDestMap = routeDestinationMap.computeIfAbsent(route,
                    routeKey -> new HashMap<>());

            var schedule = trip.getSchedule();
            var to = schedule.getToDestination();

            var existingInfo = currentDestMap.get(to);
            if(existingInfo == null) {
                currentDestMap.put(to, closestTripInfo);
            } else {
                var existingDistance = this.calculateDistance(existingInfo.trip());
                if(distanceToDestination < existingDistance) {
                    currentDestMap.put(to, closestTripInfo);
                }
            }
        }
        return routeDestinationMap;
    }



    private double calculateDistance(Trip trip) {
        var currentLocation = this.getCurrentLocation(trip);
        log.info("Current location is {}", currentLocation);
        if(currentLocation == null)
            return 0;

        var currentLocationAtLatLon = LatLon
                .of(currentLocation.longitude(),
                        currentLocation.latitude());

        var route = trip.getRoute();
        var to = trip.getSchedule().getToDestination();
        var from = trip.getSchedule().getFromDestination();

        return this.defaultRouteServiceCacheHandler.getRemainingDistanceToDestination(route, from,
                to, currentLocationAtLatLon);
    }

    private double getCurrentLocationSpeed(Trip trip) {
        var currentPosition = getCurrentLocation(trip);
        if(currentPosition == null)
            return 0.0;
        return currentPosition.speed();
    }

    private DriverCurrentLocationMessage getCurrentLocation(Trip trip) {
        var currentPosition = this.busQueues.get(trip.getId());
        if(currentPosition == null)
            return null;
        return currentPosition.peekLast();
    }

    private double roundOff(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(value * scale) / scale;
    }
}