package org.tracker.ubus.ubus.Components.Buses.BusTracking.BusJourneyTracker.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Metrics;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Handlers.DefaultRouteServiceCacheHandler;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Util.MathUtil;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;


@Slf4j
public class BusJourneyTracker {

    @Autowired
    private DefaultRouteServiceCacheHandler routeServiceCacheHandler;

    @Autowired
    private ConcurrentHashMap<UUID, ConcurrentLinkedDeque<DriverCurrentLocationMessage>> busQueues;

    public int calculateJourneyProgress(Trip trip) {

        var schedule = trip.getScheduleLegBusAssignment()
                .getScheduleLeg();

        var from = schedule.getFromDestination();
        var to = schedule.getToDestination();

        var latLonOfFrom = LatLon.of(from.getLng(), from.getLat()); //creating the start and end of the trip co-ordinates

        var route = trip.getRoute();
        var totalTripDistance = this.routeServiceCacheHandler.getRemainingDistanceToDestination(route, from, to, latLonOfFrom);

        var latLonOfCurrentLocation = this.getBusCurrentLocation(trip);
        if( latLonOfCurrentLocation == null)
            return 0;

        var remainingDistance = this.routeServiceCacheHandler.getRemainingDistanceToDestination(route, from, to, latLonOfCurrentLocation);

        var completedDistance = totalTripDistance - remainingDistance;
        var progress = (int) (completedDistance / totalTripDistance * 100);

        progress = Math.min(progress, 100);
        return progress;
    }

    protected double getDistanceLeft(Trip trip, Metrics metric) {

        var schedule = trip.getScheduleLegBusAssignment()
                .getScheduleLeg();

        var from = schedule.getFromDestination();
        var to = schedule.getToDestination();
        var route = trip.getRoute();
        var latLonOfFrom = LatLon.of(from.getLng(), from.getLat());

        var distanceInMeters = this.routeServiceCacheHandler.getRemainingDistanceToDestination(route, from, to,
                latLonOfFrom);

        if(metric == Metrics.KILOMETERS) {
            distanceInMeters = distanceInMeters / 1000;
            distanceInMeters = MathUtil.round(distanceInMeters, 2);
        }

        return distanceInMeters;
    }

    protected double getSpeedOfBus(Trip trip) {
        var currentPosition = this.busQueues.get(trip.getId());
        if(currentPosition == null || currentPosition.isEmpty())
            return 0D;
        return currentPosition.peekLast()
                .speed();
    }

    protected LatLon getBusCurrentLocation(Trip trip) {
        var driverCurrentLocationMessageQue = this.busQueues.get(trip.getId());

        if(driverCurrentLocationMessageQue == null)
            return null;

        var driverLastKnownLocation = driverCurrentLocationMessageQue.peekLast();
        if(driverLastKnownLocation == null)
            return null;

        return LatLon.builder()
                .lat(driverLastKnownLocation.latitude())
                .lon(driverLastKnownLocation.longitude())
                .build();
    }

    protected DriverCurrentLocationMessage getCurrentLocationMessage(Trip trip) {
        var currentPosition = this.busQueues.get(trip.getId());
        if(currentPosition == null)
            return null;
        return currentPosition.peekLast();
    }

}
