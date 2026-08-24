package org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Internal.ClosestTripInfo;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.UserPreferenceNearestBus;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.BusJourneyTracker.BusJourneyTracker;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Handlers.DefaultRouteServiceCacheHandler;
import org.tracker.ubus.ubus.Components.Trips.Trip.CacheManager.TripCacheManager;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Util.EtaCalculator;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class BusPreferenceTripLoader extends BusJourneyTracker {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm a");
    private final DefaultRouteServiceCacheHandler defaultRouteServiceCacheHandler;
    private final TripCacheManager manager;

    public Collection<Trip> getCurrentTrips() {
        return manager.getAll();
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


        var trip = userToDestInfo.getTrip();
        var schedule = trip.getScheduleLegBusAssignment()
                .getScheduleLeg();
        int stopsToUser = this.calculateStopsToUser(route, schedule.getFromDestination(), userToDest);
        int progress = this.calculateJourneyProgress(trip);
        boolean isUserAtStop = stopsToUser == 0;

        // Use the OLD method to calculate total stops
        int totalStopsForJourney = this.calculateJourneyStopsBetweenDestinations(route, userFromDest, userToDest);

        return UserPreferenceNearestBus.builder()
                .stops(stopsToUser)
                .from(userFromDest)
                .to(userToDest)
                .progress(progress)
                .isAtUserStop(isUserAtStop)
                .totalStopsForJourney(totalStopsForJourney)
                .nearestTrip(userToDestInfo)
                .build();

    }


    public Collection<UserPreferenceNearestBus> getNearestBusesToPreference(int totalBusesToShow, List<BusPreference> busPreferences) {

        var allTrips = this.getCurrentTrips();
        var results = new ArrayList<UserPreferenceNearestBus>();

        for (var preference : busPreferences) {
            var from = preference.getFromDestination();
            var to = preference.getToDestination();
            var user = preference.getUser();

            // Find all routes that serve this (from → to) combination
            var preferenceRoutes = Route.findRouteByDestinations(to, from);

            for (var route : preferenceRoutes) {

                // Find all buses currently on this route
                var routeTrips = allTrips.stream()
                        .filter(trip -> trip.getRoute() == route)
                        .toList();

                for (var trip : routeTrips) {

                    var legTrip = trip.getScheduleLegBusAssignment()
                            .getScheduleLeg();
                    var busCurrentLocation = legTrip.getFromDestination();
                    var busNextStop = legTrip.getToDestination();

                    // Check if bus is heading toward user's 'to' destination
                    if (!isHeadingTowardDestination(route, busCurrentLocation, busNextStop, to)) {
                        continue;
                    }

                    // Calculate distance from bus's current location to user's 'to'
                    var distanceToDestination = this.calculateDistanceToDestination(trip, to);

                    var speed = this.getCurrentLocationSpeed(trip);
                    var kmEta = roundOff2Decimals(distanceToDestination / 1000);
                    var strKM = kmEta + " km";

                    var eta = EtaCalculator.calculateETA(distanceToDestination, speed);
                    var formattedEta = eta.format(formatter);

                    var delayStatus = EtaCalculator.containsDelay(legTrip.getArrivalTime(), eta);
                    var bus = trip.getBusAssignment().getBus();
                    var progress = this.calculateJourneyProgress(trip);

                    var closestTripInfo = ClosestTripInfo.builder()
                            .eta(formattedEta)
                            .distance(strKM)
                            .distanceInKM(kmEta)
                            .busName(bus.getName())
                            .delayStatus(delayStatus)
                            .progress(progress)
                            .trip(trip)
                            .build();

                    int stopsToUser = this.calculateStopsToUser(route, busCurrentLocation, to);
                    boolean isUserAtStop = stopsToUser == 0;
                    int totalStopsForJourney = this.calculateJourneyStopsBetweenDestinations(route, from, to);

                    var result = UserPreferenceNearestBus.builder()
                            .stops(stopsToUser)
                            .user(user)
                            .from(from)
                            .to(to)
                            .progress(progress)
                            .isAtUserStop(isUserAtStop)
                            .totalStopsForJourney(totalStopsForJourney)
                            .nearestTrip(closestTripInfo)
                            .build();

                    results.add(result);
                }
            }
        }

        // Sort by distance (closest first)
        results.sort(Comparator.comparingDouble(
                bus -> bus.nearestTrip().getDistanceInKM()
        ));

        // Return top N buses (limit to totalBusesToShow)
        return results.stream()
                .limit(totalBusesToShow)
                .collect(Collectors.toList());
    }


    private double calculateDistanceToDestination(Trip trip, Destination targetDestination) {
        var currentLocation = this.getCurrentLocation(trip);
        if (currentLocation == null) {
            return 0;
        }

        var currentLocationAtLatLon = LatLon
                .of(currentLocation.longitude(),
                        currentLocation.latitude());

        var route = trip.getRoute();

        // Get the bus's current leg (where it's heading next)
        var leg = trip.getScheduleLegBusAssignment().getScheduleLeg();
        var busCurrentLocation = leg.getFromDestination();
        var busNextStop = leg.getToDestination();

        // Get unique stops for this route
        var uniqueStops = route.getUniqueStops();

        int currentIdx = uniqueStops.indexOf(busCurrentLocation);
        int targetIdx = uniqueStops.indexOf(targetDestination);
        int nextIdx = uniqueStops.indexOf(busNextStop);

        if (currentIdx == -1 || targetIdx == -1 || nextIdx == -1) {
            return 0;
        }

        // Determine direction of travel
        boolean isMovingForward = (nextIdx > currentIdx) ||
                (nextIdx == 0 && currentIdx == uniqueStops.size() - 1);

        // If the target is behind the bus, return 0 (bus is not heading toward it)
        if (!isHeadingTowardDestination(route, busCurrentLocation, busNextStop, targetDestination)) {
            return 0;
        }

        // Step 1: Distance from current position to the next stop
        double distanceToNextStop = this.defaultRouteServiceCacheHandler.getRemainingDistanceToDestination(
                route,
                busCurrentLocation,
                busNextStop,
                currentLocationAtLatLon
        );

        // Step 2: Build the list of stops from the next stop to the target
        List<Destination> stopsAfterNext = new ArrayList<>();
        List<Destination> stopsToTarget = new ArrayList<>();

        if (isMovingForward) {
            // Moving forward: collect stops from nextIdx to targetIdx (cyclic)
            for (int i = (nextIdx + 1) % uniqueStops.size(); i != currentIdx; i = (i + 1) % uniqueStops.size()) {
                stopsToTarget.add(uniqueStops.get(i));
                if (uniqueStops.get(i) == targetDestination) {
                    break;
                }
            }
        } else {
            // Moving backward: collect stops from nextIdx to targetIdx (reverse cyclic)
            for (int i = (nextIdx - 1 + uniqueStops.size()) % uniqueStops.size(); i != currentIdx; i = (i - 1 + uniqueStops.size()) % uniqueStops.size()) {
                stopsToTarget.add(uniqueStops.get(i));
                if (uniqueStops.get(i) == targetDestination) {
                    break;
                }
            }
        }

        // If the target is not on the path, return 0
        if (!stopsToTarget.contains(targetDestination)) {
            return 0;
        }

        // Step 3: Calculate distance for all remaining segments
        double remainingSegmentDistance = 0.0;

        for (int i = 0; i < stopsToTarget.size() - 1; i++) {
            Destination from = stopsToTarget.get(i);
            Destination to = stopsToTarget.get(i + 1);

            // If we've reached the target, break
            if (to == targetDestination) {
                // Get the distance for this final segment (full segment)
                var segmentCoords = defaultRouteServiceCacheHandler.getRouteSegmentCoordinates(route, from, to);
                remainingSegmentDistance += defaultRouteServiceCacheHandler.calculateTotalDistance(segmentCoords);
                break;
            }

            // Get the distance for this segment (full segment)
            var segmentCoords = defaultRouteServiceCacheHandler.getRouteSegmentCoordinates(route, from, to);
            remainingSegmentDistance += defaultRouteServiceCacheHandler.calculateTotalDistance(segmentCoords);
        }

        // Total distance = distance to next stop + all remaining segments
        return distanceToNextStop + remainingSegmentDistance;
    }


    private boolean isHeadingTowardDestination(Route route, Destination currentLocation, Destination nextStop, Destination target) {

        // Get unique stops for this route
        var uniqueStops = route.getUniqueStops();

        int currentIdx = uniqueStops.indexOf(currentLocation);
        int nextIdx = uniqueStops.indexOf(nextStop);
        int targetIdx = uniqueStops.indexOf(target);

        if (currentIdx == -1 || nextIdx == -1 || targetIdx == -1) {
            return false;
        }

        // Determine direction: moving forward means nextIdx > currentIdx OR wrapping around
        boolean isMovingForward = (nextIdx > currentIdx) ||
                (nextIdx == 0 && currentIdx == uniqueStops.size() - 1);

        if (isMovingForward) {
            // Check if target comes after currentIdx
            for (int i = (currentIdx + 1) % uniqueStops.size(); i != currentIdx; i = (i + 1) % uniqueStops.size()) {
                if (uniqueStops.get(i) == target) {
                    return true;
                }
            }
        } else {
            // Moving backward: check if target comes before currentIdx
            for (int i = (currentIdx - 1 + uniqueStops.size()) % uniqueStops.size(); i != currentIdx; i = (i - 1 + uniqueStops.size()) % uniqueStops.size()) {
                if (uniqueStops.get(i) == target) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Calculates how many stops remain between the bus's current location and the
     * user's boarding stop, on the given route.
     * <p>
     * Handles routes that revisit the same {@link Destination} on both the outbound
     * and return leg (e.g. a loop route like [DFC, APB, APK, APB, DFC]) by trying
     * every occurrence of both endpoints and taking the shortest valid path, rather
     * than assuming the first occurrence in the list is the correct one.
     */
    public int calculateStopsToUser(Route route, Destination busCurrentLocation, Destination userBoardingStop) {
        return minCyclicStops(route.getDestinations(), busCurrentLocation, userBoardingStop);
    }


    /**
     * Calculates the total number of stops for a user's journey between two
     * destinations on the given route. See {@link #calculateStopsToUser} for
     * notes on how duplicate/loop stops are handled.
     */
    public int calculateJourneyStopsBetweenDestinations(Route route, Destination userBoardingStop, Destination userDestinationStop) {
        return minCyclicStops(route.getDestinations(), userBoardingStop, userDestinationStop);
    }


    public Map<Route, Map<Destination, ClosestTripInfo>> getNearestTripsToTheirDestination() {
        var allTrips = this.getCurrentTrips();
        var routeDestinationMap = new HashMap<Route, Map<Destination, ClosestTripInfo>>();

        for (var trip : allTrips) {
            var route = trip.getRoute();
            var distanceToDestination = this.calculateDistance(trip);

            var speed = this.getCurrentLocationSpeed(trip);
            var kmEta = roundOff2Decimals(distanceToDestination / 1000);
            var strKM = kmEta + " km";

            var eta = EtaCalculator.calculateETA(distanceToDestination, speed);

            var leg = trip.getScheduleLegBusAssignment()
                    .getScheduleLeg();

            var delayStatus = EtaCalculator.containsDelay(leg.getArrivalTime(), eta);
            var formattedEta = eta.format(formatter);

            var bus = trip.getBusAssignment().getBus();

            var progress = this.calculateJourneyProgress(trip);

            var closestTripInfo = ClosestTripInfo.builder()
                    .eta(formattedEta)
                    .distance(strKM)
                    .distanceInKM(kmEta)
                    .busName(bus.getName())
                    .delayStatus(delayStatus)
                    .progress(progress)
                    .trip(trip)
                    .build();

            var currentDestMap = routeDestinationMap.computeIfAbsent(route,
                    routeKey -> new HashMap<>());

            var schedule = trip.getScheduleLegBusAssignment()
                    .getScheduleLeg();

            var to = schedule.getToDestination();

            var existingInfo = currentDestMap.get(to);
            if(existingInfo == null) {
                currentDestMap.put(to, closestTripInfo);
            } else {
                var existingDistance = this.calculateDistance(existingInfo.getTrip());
                if(distanceToDestination < existingDistance) {
                    currentDestMap.put(to, closestTripInfo);
                }
            }
        }
        return routeDestinationMap;
    }



    private List<Integer> getAllIndices(List<Destination> destinations, Destination target) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < destinations.size(); i++)
            if (destinations.get(i) == target)
                indices.add(i);
        return indices;
    }


    /**
     * Finds the minimum stop count between {@code from} and {@code to} on a route's
     * destination list, considering every occurrence of each destination (since loop
     * routes can list the same stop more than once, e.g. on the outbound and return leg).
     * <p>
     * For each pair of (fromIndex, toIndex), the stop count is computed by walking
     * forward from fromIndex to toIndex if fromIndex < toIndex, or wrapping around the
     * end of the list back to the start otherwise (matching a circular route). The
     * smallest count across all pairs is returned, since that reflects the shortest
     * physically valid path along the route.
     */
    private int minCyclicStops(List<Destination> destinations, Destination from, Destination to) {
        List<Integer> fromIndices = getAllIndices(destinations, from);
        List<Integer> toIndices = getAllIndices(destinations, to);

        if (fromIndices.isEmpty() || toIndices.isEmpty()) {
            log.warn("Destination not found in route: from={}, to={}", from, to);
            return 0;
        }

        int size = destinations.size();
        int best = Integer.MAX_VALUE;

        for (int fromIndex : fromIndices) {
            for (int toIndex : toIndices) {
                int stops = countUniqueStopsCyclic(destinations, fromIndex, toIndex, size);
                if (stops < best) {
                    best = stops;
                }
            }
        }

        log.debug("Stops from {} to {}: {}", from, to, best);
        return best;
    }

    private int countUniqueStopsCyclic(List<Destination> destinations, int startIndex, int endIndex, int size) {
        if (startIndex == endIndex) {
            return 0;
        }

        Set<Destination> uniqueStops = new LinkedHashSet<>();

        if (startIndex < endIndex) {
            // Moving forward
            for (int i = startIndex + 1; i <= endIndex; i++) {
                uniqueStops.add(destinations.get(i));
            }
        } else {
            // Moving backward (wrap around)
            for (int i = startIndex + 1; i < size; i++) {
                uniqueStops.add(destinations.get(i));
            }
            for (int i = 0; i <= endIndex; i++) {
                uniqueStops.add(destinations.get(i));
            }
        }

        return uniqueStops.size();
    }

    private double calculateDistance(Trip trip) {
        var currentLocation = this.getCurrentLocation(trip);
        if(currentLocation == null)
            return 0;

        var currentLocationAtLatLon = LatLon
                .of(currentLocation.longitude(),
                        currentLocation.latitude());

        var route = trip.getRoute();

        var to = trip.getScheduleLegBusAssignment()
                .getScheduleLeg()
                .getToDestination();

        var from = trip.getScheduleLegBusAssignment()
                .getScheduleLeg()
                .getFromDestination();

        if(from == to)
            return 0;

        return this.defaultRouteServiceCacheHandler.getRemainingDistanceToDestination(route, from,
                to, currentLocationAtLatLon);
    }

    private double getCurrentLocationSpeed(Trip trip) {
        var currentPosition = getCurrentLocation(trip);
        if(currentPosition == null)
            return 0.0;
        return currentPosition.speed();
    }

    private double roundOff2Decimals(double value) {
        double scale = Math.pow(10, 2);
        return Math.round(value * scale) / scale;
    }
}