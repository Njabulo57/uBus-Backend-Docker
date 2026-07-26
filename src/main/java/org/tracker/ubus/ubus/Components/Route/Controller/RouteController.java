package org.tracker.ubus.ubus.Components.Route.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tracker.ubus.ubus.Components.Route.DTO.Response.RouteResponse;
import org.tracker.ubus.ubus.Components.Route.Service.RouteService;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/routes")
public class RouteController {

    @Qualifier("default-route-service")
    private final RouteService routeService;

    @GetMapping("/get-all-routes")
    public Collection<RouteResponse> getAllRoutes(){
        return this.routeService.getAllRoutes();
    }


    @GetMapping("/get-all-routes-grouped")
    public Map<Route, List<RouteResponse>> getAllRoutesGrouped(){
        return this.routeService.getAllRoutesGrouped();
    }


    @GetMapping("/get-end-coordinates")
    public double[] getDestinationEndCoordinates(@RequestParam("destination") String destination) {
        return this.routeService.getDestinationCoordinates(destination);
    }

}
