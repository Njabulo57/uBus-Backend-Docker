package org.tracker.ubus.ubus.Components.Buses.BusRoute.Mappers;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusRoute.Entity.BusRoute;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

@Component
public class BusRouteMapper {


    public BusRoute toEntity(Bus bus, Route route) {
        return BusRoute.builder()
                .bus(bus)
                .route(route)
                .build();
    }
}
