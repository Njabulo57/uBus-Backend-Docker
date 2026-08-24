package org.tracker.ubus.ubus.Configuration.ExternalClients.OpenRouteService.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Configuration.ExternalClients.OpenRouteService.API.OpenRouteAPI;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenRouteService {

    @Value("${ubus.route.api.key}")
    private String apiKey;

    private final OpenRouteAPI openRouteAPI;


    public List<double[]> getRoute(double startLat, double startLng, double endLat, double endLng) {

        var start = format(startLng, startLat);
        var end = format(endLng, endLat);

        var routeCoordinates = openRouteAPI.getRoute(apiKey, start, end);
        return routeCoordinates.features()[0].geometry().coordinates();
    }

    public List<double[]> getRoute(Destination start, Destination end) {
        return getRoute(start.getLat(), start.getLng(), end.getLat(), end.getLng());
    }

    public List<double[]> getRoute(Destination start, double[] endLatLong) {
        return getRoute(start.getLat(), start.getLng(), endLatLong[0], endLatLong[1]);
    }
    
    public List<double[]> getRoute(double[] startLatLong, double[] endLatLong) {
        return getRoute(startLatLong[0], startLatLong[1], endLatLong[0], endLatLong[1]);
    }

    private String format(double lat, double lng) {
        return lng + "," + lat;
    }

}
