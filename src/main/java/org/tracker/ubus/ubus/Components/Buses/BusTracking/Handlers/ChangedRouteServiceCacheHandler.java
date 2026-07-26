package org.tracker.ubus.ubus.Components.Buses.BusTracking.Handlers;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Route.DTOs.Internal.RouteSegmentKey;
import org.tracker.ubus.ubus.Components.Trips.Trip.Util.TripMathUtil;
import org.tracker.ubus.ubus.Configuration.HttpClient.Service.RouteService;

import java.util.List;

@Component
public class ChangedRouteServiceCacheHandler {

    private final RouteService routeService;
    private final Cache<RouteSegmentKey, List<LatLon>> changedRouteCache;

    public ChangedRouteServiceCacheHandler(@Qualifier("changedBusRouteCoordinatesCache") Cache<RouteSegmentKey,
            List<LatLon>> changedRouteCache, RouteService routeService) {

        this.changedRouteCache = changedRouteCache;
        this.routeService = routeService;
    }

    public static boolean isBusOffRoute(List<LatLon> route, LatLon currentPosition, double thresholdMeters) {
        var isOffRoute = route.stream()
                .anyMatch(latLon -> {

                    var distance = TripMathUtil.haverSineDistance(latLon.lat(), latLon.lon(),
                            currentPosition.lat(), currentPosition.lon());

                    return distance < thresholdMeters;
                });
        return !isOffRoute;
    }

    public void updateSegmentCache(RouteSegmentKey key, LatLon currentPosition) {
        var startLat = currentPosition.lat();
        var startLng = currentPosition.lon();

        var endLat = key.to().getLat();
        var endLng = key.to().getLng();

        var route = this.routeService.getRoute(startLat, startLng, endLat, endLng);
        var latLongRoute = of(route);
        changedRouteCache.put(key, latLongRoute);

    }

    private List<LatLon> of(List<double[]> coordinates) {
        return coordinates.stream()
                .map(coordinate -> LatLon.builder()
                        .lat(coordinate[1])
                        .lon(coordinate[0])
                        .build()
                ).toList();
    }
}
