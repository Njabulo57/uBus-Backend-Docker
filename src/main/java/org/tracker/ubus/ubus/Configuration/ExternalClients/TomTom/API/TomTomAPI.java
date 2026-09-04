package org.tracker.ubus.ubus.Configuration.ExternalClients.TomTom.API;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.tracker.ubus.ubus.Configuration.ExternalClients.TomTom.DTOs.Response.TomTomRouteAPIResponse;

@HttpExchange(url = "https://api.tomtom.com/routing/1/calculateRoute", accept = "application/json")
public interface TomTomAPI {

    /**
     * Fetches a route with traffic information from TomTom Routing API
     *
     * @param key Your TomTom API key
     * @param query The route query (format: lat,lon:lat,lon)
     * @param traffic Enable traffic data (true/false)
     * @param routeType Route type (fastest, shortest, eco, thrilling)
     * @param travelMode Travel mode (car, bus, pedestrian, etc.)
     * @return Route response with traffic information
     */
    @GetExchange("/{query}/json")
    TomTomRouteAPIResponse.TomTomRouteResponse getRoute(
            @RequestParam("key") String key,
            @PathVariable("query") String query,
            @RequestParam(value = "traffic", defaultValue = "true") boolean traffic,
            @RequestParam(value = "routeType", defaultValue = "fastest") String routeType,
            @RequestParam(value = "travelMode", defaultValue = "car") String travelMode
    );
}