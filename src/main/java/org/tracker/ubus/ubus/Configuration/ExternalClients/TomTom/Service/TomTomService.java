package org.tracker.ubus.ubus.Configuration.ExternalClients.TomTom.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Configuration.ExternalClients.TomTom.API.TomTomAPI;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Configuration.ExternalClients.TomTom.DTOs.Response.TomTomRouteAPIResponse;


@Slf4j
@Service
@RequiredArgsConstructor
public class TomTomService {

    private final TomTomAPI tomTomAPI;

    @Value("${tom.tom.api.key}")
    private String API_KEY;

    /**
     * Get route with traffic between two destinations
     */
    public TomTomRouteAPIResponse.TomTomRouteResponse getRouteWithTraffic(Destination from, Destination to,
                                                                          boolean withTraffic) {
        String query = buildQuery(from, to);

        return tomTomAPI.getRoute(
                API_KEY,
                query,
                withTraffic,           // traffic
                "fastest",      // routeType
                "car"           // travelMode
        );
    }

    /**
     * Get route with traffic between coordinates
     */
    public TomTomRouteAPIResponse.TomTomRouteResponse getRouteWithTraffic(double fromLat, double fromLon,
                                                             double toLat, double toLon) {
        String query = buildQuery(fromLat, fromLon, toLat, toLon);

        return tomTomAPI.getRoute(
                API_KEY,
                query,
                true,
                "fastest",
                "car"
        );
    }

    private String buildQuery(Destination from, Destination to) {
        return String.format("%f,%f:%f,%f",
                from.getLat(), from.getLng(),
                to.getLat(), to.getLng());
    }

    private String buildQuery(double fromLat, double fromLon, double toLat, double toLon) {
        return String.format("%f,%f:%f,%f", fromLat, fromLon, toLat, toLon);
    }



    /**
     * Get total travel time in minutes
     */
    public int getTravelTimeMinutes(TomTomRouteAPIResponse.TomTomRouteResponse response) {
        if (response == null || response.routes() == null || response.routes().isEmpty())
            return 0;

        return response.routes().getFirst()
                .summary()
                .travelTimeInSeconds() / 60;
    }

    /**
     * Get traffic delay in minutes
     */
    public int getTrafficDelayMinutes(TomTomRouteAPIResponse.TomTomRouteResponse response) {
        if (response == null || response.routes() == null || response.routes().isEmpty()) {
            return 0;
        }
        return response.routes().getFirst().summary().trafficDelayInSeconds() / 60;
    }

    /**
     * Check if there is traffic on the route
     */
    public boolean hasTraffic(TomTomRouteAPIResponse.TomTomRouteResponse response) {
        if (response == null || response.routes() == null || response.routes().isEmpty())
            return false;

        return response.routes().getFirst()
                .summary()
                .trafficDelayInSeconds() > 0;
    }





}