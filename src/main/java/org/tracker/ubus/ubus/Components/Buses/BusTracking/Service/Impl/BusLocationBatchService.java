package org.tracker.ubus.ubus.Components.Buses.BusTracking.Service.Impl;

import jakarta.annotation.PreDestroy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Responses.DriverCurrentLocationResponse;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Mappers.BusTrackingMapper;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Service.Interface.IBusLocationBatchService;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.Repository.TripHistoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusLocationBatchService implements IBusLocationBatchService {

    private static final int MAX_QUEUE_SIZE = 5_000;
    private static final int FLUSH_BATCH_SIZE = 800;
    private static final long FLUSH_INTERVAL_MS = 300_000; // 5 minutes


    private final BusTrackingMapper busTrackingMapper;
    private final TripHistoryRepository tripHistoryRepository;
    private final TripRepository tripRepository;

    private final ConcurrentMap<UUID, BlockingQueue<DriverCurrentLocationMessage>> busQueues;
    private final ConcurrentMap<UUID, AtomicBoolean> busFlushing;
    private final ConcurrentMap<UUID, Trip> busTripCache;


    @Override
    public DriverCurrentLocationResponse enqueue(DriverCurrentLocationMessage msg) {
        var tripEntity = this.tripRepository.findByIdOrThrow(msg.tripId());
        var bus = tripEntity.getBusAssignment().getBus();
        var busId = bus.getId();

        var queue = busQueues.computeIfAbsent(busId,
                id -> new LinkedBlockingQueue<>(MAX_QUEUE_SIZE));

        // Cache the trip for this bus
        busTripCache.computeIfAbsent(busId, k -> tripEntity);


        var busLocationToDistribute = this.busTrackingMapper.toDTO(tripEntity);
        if (queue.offer(msg)) {
            // Only check size if we might need to flush
            if (queue.size() >= FLUSH_BATCH_SIZE)
                flushBus(busId, "BATCH_FULL");
            return busLocationToDistribute;
        }

        // Queue full. dropping oldest and adding new
        queue.poll();
        queue.offer(msg);
        log.warn("Bus {}: queue full, dropped oldest location", busId);

        return busLocationToDistribute;
    }

    @Override
    public void endTrip(UUID tripId) {
        // Find bus for this trip and flush
        var trip = tripRepository.findByIdOrThrow(tripId);
        var busId = trip.getBusAssignment().getBus().getId();
        flushBus(busId, "TRIP_END");
        // Clean up after ending the trip

        busQueues.remove(busId);
        busFlushing.remove(busId);
        busTripCache.remove(busId);
    }


    @Scheduled(fixedDelay = FLUSH_INTERVAL_MS)
    protected void scheduledSweep() {

        for (Map.Entry<UUID, BlockingQueue<DriverCurrentLocationMessage>> entry : busQueues.entrySet()) {
            UUID busId = entry.getKey();
            BlockingQueue<DriverCurrentLocationMessage> q = entry.getValue();

            if (!q.isEmpty())
                flushBus(busId, "INTERVAL");

        }
    }

    @PreDestroy
    protected void shutdown() {
        log.info("Shutdown: flushing {} buses", busQueues.size());
        this.busQueues.forEach((busId, q)
                -> flushBus(busId, "SHUTDOWN"));
    }


    private void flushBus(UUID busId, String reason) {
        AtomicBoolean flushing = busFlushing.computeIfAbsent(busId,
                id -> new AtomicBoolean(false));

        // Only one flusher per bus
        if (!flushing.compareAndSet(false, true)) {
            log.debug("Bus {}: flush skipped ({}), another in progress", busId, reason);
            return;
        }


        var q = busQueues.get(busId);
        if (q.isEmpty())
            return;


        // Drain all messages
        List<DriverCurrentLocationMessage> drained = new ArrayList<>();
        int drainedCount = q.drainTo(drained);

        if (drainedCount == 0)
            return;

        // Get cached trip
        Trip trip = busTripCache.get(busId);
        log.debug("Bus {}: saving {} locations ({})", busId, drainedCount, reason);

        // Save to database
        var entities = busTrackingMapper.toEntities(drained, trip);


        try
        {
            tripHistoryRepository.saveAll(entities);
            log.info("Bus {}: saved {} locations", busId, drainedCount);
        } catch (Exception e) {
            log.error("Bus {}: failed to save locations", busId, e);
        } finally {
            flushing.set(false);
        }
    }
}