package org.tracker.ubus.ubus.Components.Buses.BusTracking.Handlers;


import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Route.DTOs.Internal.RouteSegmentKey;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Util.TripMathUtil;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Component
public class DefaultRouteServiceCacheHandler {

    private final Cache<RouteSegmentKey, List<LatLon>> defaultRouteCache;

    public DefaultRouteServiceCacheHandler(@Qualifier("defaultBusRouteCoordinatesCache") Cache<RouteSegmentKey, List<LatLon>> defaultRouteCache) {
        this.defaultRouteCache = defaultRouteCache;
    }


    public Destination getNextDestination(Route route, Destination destination) {
        var destinations = List.copyOf(route.getDestinations());
        var index = destinations.indexOf(destination);
        return index == destinations.size() - 1 ? null : destinations.get(index + 1);
    }

    public Destination getPreviousDestination(Route route, Destination destination) {
        var destinations = List.copyOf(route.getDestinations());
        var index = destinations.indexOf(destination);
        return index == 0 ? null : destinations.get(index - 1);
    }



    public double getRemainingDistanceToDestination(Route route, Destination from, Destination to, LatLon currentPos) {
        // Get the coordinates for this specific segment (from -> to)
        var segmentCoordinates = getRouteSegmentCoordinates(route, from, to);

        // Find the closest point on this segment to current position
        var closestPointIndex = getClosestPoint(segmentCoordinates, currentPos);

        // Calculate remaining distance from closest point to the end of the segment
        return calculateRemainingDistance(segmentCoordinates, closestPointIndex);
    }


    public double getSegmentRemainingDistance(Route route, Destination currentDest, LatLon currentPosition) {
        var nextDest = getNextDestination(route, currentDest);
        if(nextDest == null)
            return 0;

        var segmentCoordinates = getRouteSegmentCoordinates(route, currentDest, nextDest);
        var closestPointIndex = getClosestPoint(segmentCoordinates, currentPosition);
        return calculateRemainingDistance(segmentCoordinates, closestPointIndex);
    }

    public List<LatLon> getRouteSegmentCoordinates(Route route, Destination from, Destination to) {
        var segmentKey = RouteSegmentKey.of(route, from, to);
        var coordinates = defaultRouteCache.getIfPresent(segmentKey);
        if(coordinates == null)
            throw new IllegalArgumentException("No coordinates found for route " + route.getLabel() +
                    " from " + from + " to " + to);
        return coordinates;
    }

    /**
     * Get full route coordinates by combining all segments in order
     */
    public List<LatLon> getFullRouteCoordinates(Route route) {
        var destinations = new ArrayList<>(route.getDestinations());
        var fullRoute = new ArrayList<LatLon>();

        for (int i = 0; i < destinations.size() - 1; i++) {
            var from = destinations.get(i);
            Destination to = destinations.get(i + 1);

            List<LatLon> segment = getRouteSegmentCoordinates(route, from, to);

            if (i == 0) {
                fullRoute.addAll(segment);
            } else {
                fullRoute.addAll(segment.subList(1, segment.size())); // Skip the first point of each segment to avoid duplicates
                log.debug("Added segment {} -> {} with {} points", from, to, segment.size() - 1);
            }
        }

        return fullRoute;
    }

    public int getClosestPoint(List<LatLon> route, LatLon currentPosition) {
        return IntStream.range(0, route.size()) // 0 to route.size()
                .boxed() // boxed to convert to Stream of integers
                .min(Comparator.comparingDouble(index -> {

                    var nextLat = route.get(index).lat();
                    var nextLng = route.get(index).lon();

                    return TripMathUtil.haverSineDistance(
                            currentPosition.lat(), currentPosition.lon(),
                            nextLat, nextLng);
                })
                ).orElse(0);
    }


    public double calculateRemainingDistance(List<LatLon> route, int currentIndex) {
        return IntStream.range(currentIndex, route.size() - 1)
                .mapToDouble(i -> TripMathUtil.haverSineDistance(route.get(i).lat(), route.get(i).lon(),
                        route.get(i + 1).lat(), route.get(i + 1).lon())
                ).sum();
    }


    public double calculateTotalDistance(List<LatLon> route) {

        return IntStream.range(0, route.size())
                .mapToDouble(i -> {

                    var currentLat = route.get(i).lat();
                    var currentLon = route.get(i).lon();

                    var nextLat = route.get(i + 1).lat();
                    var nextLon = route.get(i + 1).lon();

                    return TripMathUtil.haverSineDistance(currentLat, currentLon, nextLat, nextLon);
                }).sum();
    }


    public static List<LatLon> of(Collection<DriverCurrentLocationMessage> locations) {
        return locations.stream()
                .map(location -> LatLon.builder()
                        .lat(location.latitude())
                        .lon(location.longitude())
                        .build())
                .toList();
    }


    public static LatLon of(DriverCurrentLocationMessage location) {
        return LatLon.builder()
                .lat(location.latitude())
                .lon(location.longitude())
                .build();
    }
}

