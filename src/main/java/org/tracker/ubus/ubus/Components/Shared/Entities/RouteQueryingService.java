package org.tracker.ubus.ubus.Components.Shared.Entities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Configuration.HttpClient.API.RouteAPI;

import java.util.List;

public abstract class RouteQueryingService {

    @Value("${ubus.route.api.key}")
    private String apiKey;

    @Autowired
    private RouteAPI routeAPI;

    @Autowired
    private MultiEvenPublisher multiEvenPublisher;


    protected List<double[]> fetchRouteCoOrdinates(Trip trip, Schedule schedule) {
        Destination fromDest = schedule.getFromDestination();
        Destination toDest = schedule.getToDestination();

        String start = fromDest.getLng() + "," + fromDest.getLat();
        String end = toDest.getLng() + "," + toDest.getLat();

        var response = this.routeAPI.getRoute(apiKey, start, end);
        List<double[]> coordinates = response.features()[0].geometry().coordinates();
        return coordinates;
    }

}
