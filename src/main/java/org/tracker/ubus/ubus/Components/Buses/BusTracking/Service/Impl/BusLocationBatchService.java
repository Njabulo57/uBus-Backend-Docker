package org.tracker.ubus.ubus.Components.Buses.BusTracking.Service.Impl;


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Handlers.DefaultRouteServiceCacheHandler;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Responses.DriverCurrentLocationResponse;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Event.Socket.BusTrackingLocationDeliveryEvent;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Mappers.BusTrackingMapper;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Service.Interface.IBusLocationBatchService;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Util.EtaCalculator;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusLocationBatchService implements IBusLocationBatchService {

    private static final long ETA_UPDATE_INTERVAL_MS = 1500;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm a");

    private final TripRepository tripRepository;
    private final BusTrackingMapper busTrackingMapper;
    private final MultiEvenPublisher multiEvenPublisher;
    private final DefaultRouteServiceCacheHandler defaultRouteServiceCacheHandler;

    private final Cache<UUID, Trip> tripCache;
    private final Cache<UUID, DelayStatus> latestBusEtaCache;

    private final ConcurrentMap<UUID, ConcurrentLinkedDeque<DriverCurrentLocationMessage>> busQueues;



    @Override
    public void enqueue(DriverCurrentLocationMessage msg) {
        var tripEntity = this.tripCache.get(msg.tripId(),
                this.tripRepository::findByIdOrThrow);

        var queue = busQueues.computeIfAbsent(tripEntity.getId(),
                id -> new ConcurrentLinkedDeque<>());
        queue.offer(msg);
    }


    @Scheduled(fixedDelay = ETA_UPDATE_INTERVAL_MS)
    protected void scheduledETAUpdate() {
       this.shareEtaInformation();
    }


    private DriverCurrentLocationResponse buildDriverLocationResponse(Trip trip, DriverCurrentLocationMessage msg) {

        var schedule = trip.getSchedule();
        var tripId = trip.getId();
        var route = trip.getRoute();

        var currentPos = DefaultRouteServiceCacheHandler.of(msg);

        // Get the route destinations
        List<Destination> routeDestinations = route.getDestinations();

        // Get the current destination index from the message
        int currentDestIndex = msg.currentDestIndex();
        var currentDest = routeDestinations.get(currentDestIndex);

        // Find the next destination that is different from current
        int nextIndex = (currentDestIndex + 1) % routeDestinations.size();
        Destination nextDest = routeDestinations.get(nextIndex);

        // Skip if same as current
        while (nextDest == routeDestinations.get(currentDestIndex)) {
            nextIndex = (nextIndex + 1) % routeDestinations.size();
            nextDest = routeDestinations.get(nextIndex);
        }

        // Calculate remaining distance from current position to the next destination
        double remainingDistance = this.defaultRouteServiceCacheHandler
                .getRemainingDistanceToDestination(route,
                        routeDestinations.get(currentDestIndex),
                        nextDest,
                        currentPos);

        // Calculate ETA based on remaining distance and current speed
        var eta = EtaCalculator.calculateETA(remainingDistance, msg.speed());

        // Use schedule's arrival time or estimated time to reach next destination
        var delayStatus = EtaCalculator.containsDelay(schedule.getArrivalTime(), eta);
        this.latestBusEtaCache.put(tripId, delayStatus);
        var formattedETA = eta.format(formatter);

        if(msg.isMadeIt())
            this.busQueues.get(tripId)
                    .clear();

        return this.busTrackingMapper.toDTO(trip, msg, formattedETA, delayStatus, currentDest, nextDest);
    }

    private DriverCurrentLocationResponse buildUIDriverLocationResponse(Trip trip, DriverCurrentLocationMessage msg) {
        var schedule = trip.getSchedule();
        var tripId = trip.getId();
        var route = trip.getRoute();

        // Get from and to destinations directly from schedule
        var from = schedule.getFromDestination();
        var to = schedule.getToDestination();

        var currentPos = DefaultRouteServiceCacheHandler.of(msg);

        // Calculate remaining distance from current position to the destination (to)
        double remainingDistance = this.defaultRouteServiceCacheHandler
                .getRemainingDistanceToDestination(route, from, to, currentPos);

        // Calculate ETA based on remaining distance and current speed
        var eta = EtaCalculator.calculateETA(remainingDistance, msg.speed());

        // Use schedule's arrival time or estimated time to reach destination
        var delayStatus = EtaCalculator.containsDelay(schedule.getArrivalTime(), eta);
        this.latestBusEtaCache.put(tripId, delayStatus);
        var formattedETA = eta.format(formatter);

        if(msg.isMadeIt())
            this.busQueues.get(tripId).clear();

        return this.busTrackingMapper.toDTO(trip, msg, formattedETA, delayStatus, from, to);
    }


    private void shareEtaInformation() {
        if(this.busQueues.isEmpty())
            return;

        var locations = this.busQueues.entrySet()
                .stream()
                .filter(queue -> !queue.getValue().isEmpty())
                .filter(queue -> queue.getValue().peekLast() != null)
                .filter(queue -> queue.getValue()
                        .peekLast().speed() > 0)

                .map(entry -> {
                    var trip = this.tripCache.getIfPresent(entry.getKey());

                    log.info("Sending location for trip {}", trip.getId());
                    if(trip == null) throw new IllegalStateException("Trip Ended");

                    var queue = entry.getValue();
                    var lastMsg = queue.peekLast();

                    if(lastMsg.currentDestIndex() != -5)
                        return this.buildDriverLocationResponse(trip, lastMsg);
                    else
                        return this.buildUIDriverLocationResponse(trip, lastMsg);

                }).filter(Objects::nonNull)
                .toArray(DriverCurrentLocationResponse[]::new);

        if(locations.length > 0)
            this.multiEvenPublisher.publish(() -> new BusTrackingLocationDeliveryEvent(this, locations));
    }
}