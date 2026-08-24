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


    public RouteService(RouteMapper routeMapper,
                        @Qualifier("defaultBusRouteCoordinatesCache") Cache<RouteSegmentKey,
                                List<LatLon>> routeCache) {
        this.routeMapper = routeMapper;
        this.routeCoordinate = RouteCoordinate.values();
        this.routeCache = routeCache;
    }

    @PostConstruct
    protected void init() {
        this.initializeRouteSegmentCache();
        log.info("OpenRouteService initialized");
        log.info("OpenRouteService initialized with {} routes", routeCoordinate.length);
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
}

