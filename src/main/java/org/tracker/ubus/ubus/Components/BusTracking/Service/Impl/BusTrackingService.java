package org.tracker.ubus.ubus.Components.BusTracking.Service.Impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.BusTracking.DTO.Responses.DriverCurrentLocationResponse;
import org.tracker.ubus.ubus.Components.BusTracking.Mappers.BusTrackingMapper;
import org.tracker.ubus.ubus.Components.BusTracking.Service.Interface.IBusTrackingService;
import org.tracker.ubus.ubus.Components.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trip.Entity.Trip;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class
BusTrackingService implements IBusTrackingService {

    private final TripRepository tripRepository;
    private final BusTrackingMapper busTrackingMapper;
    private final BusTrackingBatchSavingManager busTrackingSaver;

    private final ConcurrentHashMap<UUID, ConcurrentLinkedQueue<DriverCurrentLocationMessage>> busQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, AtomicInteger> busBatchSizes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> busLastFlushTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Trip> busTripCache = new ConcurrentHashMap<>();

    private static final int BATCH_SIZE = 100;
    private static final int BATCH_TIMEOUT_SECONDS = 30;

    private ScheduledExecutorService scheduler;

    @PostConstruct
    protected void startScheduler() {

        // creating one separate thread whose role is to save
        this.scheduler = Executors.newScheduledThreadPool(1,
                Thread.ofVirtual()
                        .name("batch-scheduler-", 0)
                        .factory()
        );

        this.scheduler.scheduleAtFixedRate(() -> {
            log.debug("Scheduler checking for timeouts");
            busTrackingSaver.checkAllBusesForTimeOut(
                    BATCH_TIMEOUT_SECONDS,
                    busQueues,
                    busBatchSizes,
                    busLastFlushTime,
                    busTripCache
            );
        }, BATCH_TIMEOUT_SECONDS, BATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        log.info("Scheduler started on thread {} ", Thread.currentThread());
        log.info("Batch size: {}, Timeout: {} seconds", BATCH_SIZE, BATCH_TIMEOUT_SECONDS);
    }


    @PreDestroy
    @Transactional
    protected void shutdown() {
        log.info("Shutting down, flushing remaining batches");

        for (UUID busId : busQueues.keySet()) {
            busTrackingSaver.flushBusBatch(
                    busId,
                    busQueues,
                    busBatchSizes,
                    busLastFlushTime,
                    busTripCache.get(busId),
                    "server shutdown"
            );
        }

        if (scheduler != null)
            scheduler.shutdown();

    }


    @Override
    @Transactional
    public DriverCurrentLocationResponse queueLocationForItsBatch(DriverCurrentLocationMessage location) {
        var trip = this.tripRepository.findByIdOrThrow(location.tripId());
        var busAssignment = trip.getBusAssignment();
        var bus = busAssignment.getBus();
        UUID busId = bus.getId();

        // Cache trip for this bus
        busTripCache.putIfAbsent(busId, trip);

        // Get or create queue for this bus
        var busQueue = busQueues.computeIfAbsent(busId,
                k -> new ConcurrentLinkedQueue<>());

        // Get or create size counter
        var sizeCounter = busBatchSizes.computeIfAbsent(busId,
                k -> new AtomicInteger(0));

        // Set initial flush time if this is the first location
        busLastFlushTime.putIfAbsent(busId, System.currentTimeMillis());

        busQueue.offer(location);
        int currentSize = sizeCounter.incrementAndGet();

        log.info("Queue for bus {} now has {} locations", bus.getName(), currentSize);

        if (currentSize >= BATCH_SIZE) {

            log.info("Saving Locations for bus {}. Locations reached {}", bus.getName(), currentSize);
            busTrackingSaver.flushBusBatch(
                    busId,
                    busQueues,
                    busBatchSizes,
                    busLastFlushTime,
                    trip,
                    "batch full (" + currentSize + " locations)"
            );
        }

        return this.busTrackingMapper.toDTO(trip);
    }

}