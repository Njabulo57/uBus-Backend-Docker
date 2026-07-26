package org.tracker.ubus.ubus.Components.Route.Mapper;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Enum.RouteCoordinate;
import org.tracker.ubus.ubus.Components.Route.DTO.Response.RouteResponse;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RouteMapper {


    public List<RouteResponse> toDTOs(RouteCoordinate[] routeCoordinates) {
        return Stream.of(routeCoordinates)
                .map( routeCoordinate -> RouteResponse.builder()
                        .route(routeCoordinate.getRoute())
                        .fromDestination(routeCoordinate.getFrom())
                        .toDestination(routeCoordinate.getTo())
                        .coordinates(routeCoordinate.getCoordinates())
                        .build())
                .toList();
    }


    public Map<Route, List<RouteResponse>> toGroupedDTOs(RouteCoordinate[] routeCoordinates) {
        return Stream.of(routeCoordinates)
                .collect(Collectors.groupingBy(
                        RouteCoordinate::getRoute,
                        Collectors.mapping(
                                rc -> RouteResponse.builder()
                                        .route(rc.getRoute())
                                        .fromDestination(rc.getFrom())
                                        .toDestination(rc.getTo())
                                        .coordinates(rc.getCoordinates())
                                        .build(),
                                Collectors.toList()
                        ))
                );
    }
}
