package org.tracker.ubus.ubus.Components.Buses.BusTracking.BusPromixityChecker.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.BusPromixityChecker.Event.BusArrivedAtDestinationEvent;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.Trips.Trip.CacheManager.TripCacheManager;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Util.TripMathUtil;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;


@Slf4j
@Component
@RequiredArgsConstructor
public class BusDestinationProximityChecker {

    private static final long FAST_PING_INTERVAL_MS = 3000; // 3 seconds
    private static final long SLOW_PING_INTERVAL_MS = 9000; // 10 seconds
    private static final long DISTANCE_THRESHOLD_METERS = 300;
    private static final long DISTANCE_ARRIVED_THRESHOLD_METERS = 120;

    private final MultiEvenPublisher publisher;
    private final Set<UUID> busProximityStatus;
    private final TripCacheManager tripCacheManager;
    private final Map<UUID, ConcurrentLinkedDeque<DriverCurrentLocationMessage>> busQueues;


    @Scheduled(fixedDelay = SLOW_PING_INTERVAL_MS)
    protected void checkSlowPingProximity() {

        var trips = this.tripCacheManager.getAllFromTripSimulationCacheAsCollection();

        trips.forEach(trip -> {

            var isCloseToDestination = this.isBusNearDestination(trip, DISTANCE_THRESHOLD_METERS); // check if the bus is near the destination
            var isTripMonitoredClosely = this.busProximityStatus.contains(trip.getId()); //check if the bus is already pinging

            //check if the bus is near the destination to start pinging faster
            if (isCloseToDestination && !isTripMonitoredClosely) {
                busProximityStatus.add(trip.getId()); //assign that the bus is near the destination

                log.info("Bus {} is near the destination", trip.getBusAssignment().getBus().getName());
            }
        });
    }

    @Scheduled(fixedDelay = FAST_PING_INTERVAL_MS)
    protected void checkFastPingProximity() {
        if(this.busProximityStatus.isEmpty())
            return;

        var tripsAtDestination = this.busProximityStatus.stream()
                .map(this.tripCacheManager::getFromTripSimulationCacheOrThrow) //getFromTripSimulationCache the trip entity from the cache
                .filter(trip -> this.isBusNearDestination(trip, DISTANCE_ARRIVED_THRESHOLD_METERS)) // check if the bus is near the destination

                .peek(trip -> this.busProximityStatus.remove(trip.getId())) // remove the trip destination bus from the proximity status
                .toList();

        if(!tripsAtDestination.isEmpty())
            this.publisher.publish(() -> new BusArrivedAtDestinationEvent(this, tripsAtDestination));
    }



    private boolean isBusNearDestination(Trip trip, long thresholdMeters) {

        var schedule = trip.getSchedule();
        var destination = schedule.getToDestination();

        var destLat = destination.getLat();
        var destLon = destination.getLng();

        var busCurrentLocation = this.getBusCurrentLocation(trip);
        if(busCurrentLocation == null)
            return false;

        var currentLat = busCurrentLocation.lat();
        var currentLon = busCurrentLocation.lon();

        double distanceToDestination = TripMathUtil.haverSineDistance(currentLat, currentLon, destLat, destLon);
        return distanceToDestination < thresholdMeters;
    }

    private LatLon getBusCurrentLocation(Trip trip) {
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

}
