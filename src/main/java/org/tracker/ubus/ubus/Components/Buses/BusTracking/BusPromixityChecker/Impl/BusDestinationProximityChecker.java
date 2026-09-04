package org.tracker.ubus.ubus.Components.Buses.BusTracking.BusPromixityChecker.Impl;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.tracker.ubus.ubus.Components.Buses.BusTracking.BusJourneyTracker.Impl.BusJourneyTracker;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.Publisher.MultiEventPublisher;
import org.tracker.ubus.ubus.Components.Trips.Trip.CacheManager.TripCacheManager;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Util.TripMathUtil;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Event.UserOnTripEvent;

import java.util.*;

import static org.springframework.data.geo.Metrics.KILOMETERS;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusDestinationProximityChecker extends BusJourneyTracker {

    private final String fastPingMessage = "Almost There";
    private final String slowPingMessage = "Getting There.Sit Back and Relax";


    private static final long FAST_PING_INTERVAL_MS = 2_000; // 3 seconds
    private static final long SLOW_PING_INTERVAL_MS = 6_000; // 10 seconds
    private static final long DISTANCE_THRESHOLD_METERS = 300; // 300 meters

    private final MultiEventPublisher publisher;
    private final Set<UUID> busProximityStatus;
    private final TripCacheManager tripCacheManager;

    private final Cache<UUID, DelayStatus> busDelayStatusCache;




    @Scheduled(fixedDelay = SLOW_PING_INTERVAL_MS)
    protected void checkSlowPingProximity() {

        var trips = this.tripCacheManager.getAllFromTripDemonstrationCacheAsCollection();

        trips.forEach(trip -> {

            var isCloseToDestination = this.isBusNearDestination(trip); // check if the bus is near the destination
            var isTripMonitoredClosely = this.busProximityStatus.contains(trip.getId()); //check if the bus is already pinging


            //check if the bus is near the destination to start pinging faster
            if (isCloseToDestination && !isTripMonitoredClosely) {
                busProximityStatus.add(trip.getId()); //assign that the bus is near the destination

            }else {


                var delayStatus = this.busDelayStatusCache.getIfPresent(trip.getId());

                if(delayStatus != null) {

                    var speed = this.getSpeedOfBus(trip);
                    var distance = this.getDistanceLeft(trip, KILOMETERS);
                    var progress = this.calculateJourneyProgress(trip);

                    var usersTappedIn = new UserOnTripEvent(this, slowPingMessage, false,
                            trip, delayStatus,
                            speed, progress, distance);
                    this.publisher.publish(usersTappedIn);
                }
            }
        });


    }

    @Scheduled(fixedDelay = FAST_PING_INTERVAL_MS)
    protected void checkFastPingProximity() {
        if(this.busProximityStatus.isEmpty())
            return;

        this.busProximityStatus.forEach(tripId -> {

                    var trip = this.tripCacheManager.getTripFromDemonstrationCache(tripId);
                    if(trip == null)
                        return;

                    var progress = this.calculateJourneyProgress(trip);

                    String messageWithLocation = this.formatArrivalMessage(trip);
                    // if the bus is close to destination or has arrived
                    String messageToUse = progress >= 90 ? messageWithLocation : fastPingMessage;
                    var delayStatus = this.busDelayStatusCache.getIfPresent(tripId);

                    if(delayStatus != null) {
                        boolean isCompleted = progress >= 95;

                        if(isCompleted)
                            progress = 100;

                        var speed = this.getSpeedOfBus(trip);
                        var distance = this.getDistanceLeft(trip, KILOMETERS);

                        var usersTappedIn = new UserOnTripEvent(this, messageToUse, isCompleted,
                                trip, delayStatus, speed, progress, distance);

                        this.publisher.publish(() -> usersTappedIn);
                    }
                });
    }


    private boolean isBusNearDestination(Trip trip) {

        var schedule = trip.getScheduleLegBusAssignment()
                .getScheduleLeg();

        var destination = schedule.getToDestination();

        var destLat = destination.getLat();
        var destLon = destination.getLng();

        var busCurrentLocation = this.getBusCurrentLocation(trip);
        if(busCurrentLocation == null)
            return false;

        var currentLat = busCurrentLocation.lat();
        var currentLon = busCurrentLocation.lon();

        double distanceToDestination = TripMathUtil.haverSineDistance(currentLat, currentLon, destLat, destLon);
        return distanceToDestination < BusDestinationProximityChecker.DISTANCE_THRESHOLD_METERS;
    }

    private String formatArrivalMessage(Trip trip) {
        var schedule = trip.getScheduleLegBusAssignment()
                .getScheduleLeg();
        var destination = schedule.getToDestination();
        return "Arrived At " + destination.getLabel() + ".Don't forget to tap out of the bus";
    }
}
