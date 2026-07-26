package org.tracker.ubus.ubus.Components.Route.Service;


import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Enum.RouteCoordinate;
import org.tracker.ubus.ubus.Components.Route.DTO.Response.RouteResponse;
import org.tracker.ubus.ubus.Components.Route.DTOs.Internal.RouteSegmentKey;
import org.tracker.ubus.ubus.Components.Route.Mapper.RouteMapper;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.util.*;

@Slf4j
@Service("default-route-service")
public class RouteService {


    private final RouteMapper routeMapper;
    private final RouteCoordinate[] routeCoordinate;
    private final Cache<RouteSegmentKey, List<LatLon>> routeCache;
    private final Map<Route, Map<SequencedCollection<Destination>, Integer>> routeDestinationStopsMap;

    public RouteService(RouteMapper routeMapper,
                        @Qualifier("defaultBusRouteCoordinatesCache") Cache<RouteSegmentKey,
                                List<LatLon>> routeCache, Map<Route, Map<SequencedCollection<Destination>, Integer>> destinationStopsMap) {
        this.routeMapper = routeMapper;
        this.routeDestinationStopsMap = destinationStopsMap;
        this.routeCoordinate = RouteCoordinate.values();
        this.routeCache = routeCache;
    }

    @PostConstruct
    protected void init() {
        this.initializeRouteSegmentCache();
        log.info("RouteService initialized");
        log.info("RouteService initialized with {} routes", routeCoordinate.length);
        log.info("Cached {} route segments", routeCache.estimatedSize());
    }


    public List<RouteResponse> getAllRoutes() {
        return routeMapper.toDTOs(routeCoordinate);
    }

    public Map<Route, List<RouteResponse>> getAllRoutesGrouped() {
        return routeMapper.toGroupedDTOs(routeCoordinate);
    }

    public double[] getDestinationCoordinates(String destination) {
        return Destination.valueOf(destination)
                .getLatLong();
    }

    private void initializeRouteSegmentCache() {
        //populate the cache on startup of object
        Arrays.stream(routeCoordinate)
                .forEach(rc -> {
                    var routeSegmentKey = RouteSegmentKey.builder()
                            .route(rc.getRoute())
                            .to(rc.getTo())
                            .from(rc.getFrom())
                            .build();

                    this.routeCache.put(routeSegmentKey, rc.getCoordinates());
                });
    }


    private void initRouteStops() {
        log.info("========== STARTING ROUTE DESTINATION MAP INITIALIZATION ==========");

        // Initialize for each route
        for (Route route : Route.ALL_ROUTES) {
            log.info("--------------------------------------------------");
            log.info("Processing Route: {} ({})", route.getLabel(), route);

            Map<SequencedCollection<Destination>, Integer> destinationStopsMap = new HashMap<>();
            List<Destination> destinations = route.getDestinations();

            log.info("Full route destinations: {}", destinations);
            log.info("Is Direct Route: {}", route.isDirect());

            // Get all possible destination pairs (from -> to) and calculate stops
            List<Destination> uniqueDestinations = destinations.stream()
                    .distinct()
                    .toList();

            log.info("Unique destinations: {}", uniqueDestinations);

            int pairCount = 0;
            for (int i = 0; i < uniqueDestinations.size(); i++) {
                for (int j = 0; j < uniqueDestinations.size(); j++) {
                    if (i != j) {
                        pairCount++;
                        Destination from = uniqueDestinations.get(i);
                        Destination to = uniqueDestinations.get(j);

                        // Get the full path with intermediate stops
                        SequencedCollection<Destination> path = getStopsBetween(destinations, from, to);

                        // Calculate number of stops (intermediate destinations)
                        int stops = path.size() - 2; // Excluding start and end
                        if (stops < 0) stops = 0; // Direct connection

                        log.info("  Pair #{}: {} -> {} | Path: {} | Stops: {}",
                                pairCount, from, to, path, stops);

                        destinationStopsMap.put(path, stops);
                    }
                }
            }

            log.info("Total destination pairs processed: {}", pairCount);
            log.info("Map size for route {}: {}", route.getLabel(), destinationStopsMap.size());

            routeDestinationStopsMap.put(route, destinationStopsMap);
        }

        log.info("==========================================================");
        log.info("INITIALIZATION COMPLETE. Total routes processed: {}", Route.ALL_ROUTES.size());
        log.info("==========================================================");

        // Log the final structure
        logFinalMapStructure(routeDestinationStopsMap);
    }


    /**
     * Gets the full path with intermediate stops between two destinations
     */
    private SequencedCollection<Destination> getStopsBetween(List<Destination> destinations,
                                                             Destination from,
                                                             Destination to) {
        int fromIndex = destinations.indexOf(from);
        int toIndex = destinations.indexOf(to);

        if (fromIndex == -1 || toIndex == -1) {
            return List.of(); // Empty list if not found
        }

        if (fromIndex < toIndex) {
            return new ArrayList<>(destinations.subList(fromIndex, toIndex + 1));
        } else {
            List<Destination> reversed = new ArrayList<>(destinations.subList(toIndex, fromIndex + 1));
            Collections.reverse(reversed);
            return reversed;
        }
    }

    /**
     * Logs the complete map structure for verification
     */
    private void logFinalMapStructure(Map<Route, Map<SequencedCollection<Destination>, Integer>> map) {
        log.info("");
        log.info("========== FINAL MAP STRUCTURE ==========");

        for (Map.Entry<Route, Map<SequencedCollection<Destination>, Integer>> routeEntry : map.entrySet()) {
            Route route = routeEntry.getKey();
            Map<SequencedCollection<Destination>, Integer> stopsMap = routeEntry.getValue();

            log.info("");
            log.info("Route: {} ({})", route.getLabel(), route);
            log.info("  Destinations: {}", route.getDestinations());
            log.info("  Number of destination pairs: {}", stopsMap.size());

            // Log each pair with its stops
            for (Map.Entry<SequencedCollection<Destination>, Integer> pairEntry : stopsMap.entrySet()) {
                SequencedCollection<Destination> path = pairEntry.getKey();
                Integer stops = pairEntry.getValue();
                log.info("    Path: {} -> Stops: {}", path, stops);
            }
        }

        log.info("==========================================");
        log.info("");
    }


    /**
     * Calculates the number of stops between two destinations in a route.
     *
     * @param destinations The full route destinations list
     * @param from         The starting destination
     * @param to           The ending destination
     * @return The number of stops (distance - 1), or -1 if the route doesn't connect them directly
     */
    private int calculateStops(List<Destination> destinations, Destination from, Destination to) {
        // Find indices of from and to
        int fromIndex = -1;
        int toIndex = -1;

        for (int i = 0; i < destinations.size(); i++) {
            if (destinations.get(i).equals(from) && fromIndex == -1) {
                fromIndex = i;
            }
            if (destinations.get(i).equals(to) && toIndex == -1) {
                toIndex = i;
            }
        }

        // If either destination not found
        if (fromIndex == -1 || toIndex == -1) {
            return -1;
        }

        // Calculate stops (distance between indices - 1)
        // For example: [A, B, C] from A to C = 2 stops (B, C)
        // For direct route from A to B = 0 stops (they're adjacent)
        return Math.abs(toIndex - fromIndex) - 1;
    }
}

