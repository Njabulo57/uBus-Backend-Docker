package org.tracker.ubus.ubus.Configuration.ExternalClients.OpenRouteService.API;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.tracker.ubus.ubus.Configuration.ExternalClients.OpenRouteService.DTOs.Request.GetDirectionAPIResponses;


@HttpExchange(url = "https://api.openrouteservice.org/v2/directions/driving-car", accept = "application/geo+json")
public interface OpenRouteAPI {

    /**
     * Fetches a route based on the given start and end coordinates using the OpenRouteService API.
     *
     * @param apiKey the API key string used for authorization in the request header
     * @param start the starting point coordinates in "longitude,latitude" format
     * @param end the ending point coordinates in "longitude,latitude" format
     * @return a {@link GetDirectionAPIResponses.RouteResponse} containing the route details such as features and geometry
     */
    @GetExchange
    GetDirectionAPIResponses.RouteResponse getRoute(@RequestHeader("Authorization") String apiKey,
                                                    @RequestParam String start,
                                                    @RequestParam String end);

}
