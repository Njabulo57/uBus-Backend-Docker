package org.tracker.ubus.ubus.Components.Buses.BusTracking.Service.Impl;


import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.BusPromixityChecker.Impl.BusDestinationProximityChecker;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Event.Socket.BusTrackingLocationDeliveryEvent;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Handlers.DefaultRouteServiceCacheHandler;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Responses.DriverCurrentLocationResponse;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Mappers.BusTrackingMapper;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Service.Interface.IBusLocationTrackingService;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.Publisher.MultiEventPublisher;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Util.EtaCalculator;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.DTO.Internal.TrafficInfoCarrier;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.TrafficLookUp.TrafficLookUp;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusLocationTrackingServiceService implements IBusLocationTrackingService {

    private final TripRepository tripRepository;
    private final BusTrackingMapper busTrackingMapper;
    private final MultiEventPublisher multiEventPublisher;

    private static final long DEFAULT_ETA_SPEEDS = 5;
    private static final double DEFAULT_ETA_SPEED = 50;
    private static final long BUS_LOCATION_BATCH_SIZE = 50;
    private static final long ETA_UPDATE_INTERVAL_MS = 1_500;


    private final Cache<UUID, Trip> tripCache;
    private final Cache<UUID, DelayStatus> latestBusEtaCache;


    private final TrafficLookUp trafficLookUp;
    private final BusDestinationProximityChecker busProximityChecker;
    private final DefaultRouteServiceCacheHandler defaultRouteServiceCacheHandler;
    private final ConcurrentMap<UUID, ConcurrentLinkedDeque<DriverCurrentLocationMessage>> busQueues;


    @Override
    public void enqueue(DriverCurrentLocationMessage msg) {
        var tripEntity = this.tripCache.get(msg.tripId(),
                this.tripRepository::findByIdOrThrow);

        var queue = busQueues.computeIfAbsent(tripEntity.getId(),
                id -> new ConcurrentLinkedDeque<>());

        queue.offer(msg); //adding the location for processing
        //managing the queue size
        if (queue.size() >= BUS_LOCATION_BATCH_SIZE)
            queue.poll();

    }


    @Scheduled(fixedDelay = ETA_UPDATE_INTERVAL_MS)
    protected void scheduledETAUpdate() {
        if (this.busQueues.isEmpty())
            return;

        var locations = this.busQueues.entrySet()
                .stream()
                .filter(queue -> !queue.getValue().isEmpty())
                .filter(queue -> queue.getValue().peekLast() != null)
                .map(entry -> {
                    var trip = this.tripCache.getIfPresent(entry.getKey());
                    if (trip == null)
                        return null;

                    var queue = entry.getValue();
                    var lastMsg = queue.peekLast();

                    return this.buildDriverLocationResponse(trip, lastMsg);

                }).filter(Objects::nonNull)
                .toArray(DriverCurrentLocationResponse[]::new);

        if (locations.length > 0)
            this.multiEventPublisher.publish(() -> new BusTrackingLocationDeliveryEvent(this, locations));
    }


    private DriverCurrentLocationResponse buildDriverLocationResponse(Trip trip, DriverCurrentLocationMessage msg) {

        var route = trip.getRoute();

        var schedule = trip.getScheduleLegBusAssignment()
                .getScheduleLeg();

        var from = schedule.getFromDestination();
        var to = schedule.getToDestination();



        var currentPos = DefaultRouteServiceCacheHandler.of(msg); //wrapping the current position lat and long
        var remainingDistance = this.defaultRouteServiceCacheHandler
                .getRemainingDistanceToDestination(route, from, to, currentPos);


        LocalTime eta;
        DelayStatus delayStatus;
        delayStatus = this.trafficLookUp.getDelayStatusFromTrafficIfExists(trip);

        if(delayStatus == null) {
            eta = this.getETA(msg.speed(), remainingDistance, trip);
            delayStatus = EtaCalculator.containsDelay(schedule.getArrivalTime(), eta);
        }


        if (msg.isMadeIt())
            this.clearBusCachedState(trip);

        this.latestBusEtaCache.put(trip.getId(), delayStatus);
        var progress = this.busProximityChecker.calculateJourneyProgress(trip);
        return this.busTrackingMapper.toDTO(trip, msg, delayStatus, progress);
    }



    private LocalTime getETA(double currentSpeed, double remainingDistance, Trip trip) {
        LocalTime eta;

        if(currentSpeed > 0)
            eta =  EtaCalculator.calculateETA(remainingDistance, currentSpeed);
        else {

            var tripId = trip.getId();
            var latestDelay = this.latestBusEtaCache.getIfPresent(tripId);

            //return the previously saved eta if it exists
            if(latestDelay != null)
                return latestDelay.eta();
            else {

                var deq = busQueues.get(tripId);
                var averageSpeed = this.calculateAverageSpeedFromQueue(deq);
                eta = EtaCalculator.calculateETA(remainingDistance, averageSpeed);

                var arrivalTime = trip.getScheduleLegBusAssignment()
                        .getScheduleLeg()
                        .getArrivalTime();

                var delay = EtaCalculator.containsDelay(arrivalTime, eta);
                this.latestBusEtaCache.put(tripId,delay); //cache the latest eta
            }
        }
        return eta;
    }


    private double calculateAverageSpeedFromQueue(ConcurrentLinkedDeque<DriverCurrentLocationMessage> deq) {

        var locations = new ArrayList<>(deq);
        if(locations.isEmpty() || locations.size() < 3)
            return DEFAULT_ETA_SPEED; //using the default speed if the queue is empty or less than 3 locations

        double totalSpeed = 0;

        int speedCount = (int) Math.min(locations.size(), DEFAULT_ETA_SPEEDS); //get the smallest one between que

        int startIndex = locations.size() - speedCount;
        //calculate from the last locations going backwards to the speedCount
        for (int i = startIndex; i < locations.size(); i++)
            totalSpeed += locations.get(i)
                    .speed();

        return totalSpeed / speedCount; //average the speeds
    }

    
    private void clearBusCachedState(Trip trip) {

        this.latestBusEtaCache.invalidate(trip.getId());
        this.busQueues.remove(trip.getId());
    }
}
