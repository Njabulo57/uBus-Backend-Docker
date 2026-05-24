package org.tracker.ubus.ubus.Components.BusTracking.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.BusTracking.DTO.Requests.BusCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.BusTracking.Mappers.BusTrackingMapper;
import org.tracker.ubus.ubus.Components.Trip.Entity.Trip;

import org.tracker.ubus.ubus.Components.TripHistory.Entity.TripHistoryPoint;
import org.tracker.ubus.ubus.Components.TripHistory.Repository.TripHistoryRepository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusTrackingBatchSavingManager {

    private final BusTrackingMapper busTrackingMapper;
    private final TripHistoryRepository tripHistoryRepository;

    public void checkAllBusesForTimeOut(
            int timeoutSeconds,
            ConcurrentHashMap<UUID, ConcurrentLinkedQueue<BusCurrentLocationMessage>> busQueues,
            ConcurrentHashMap<UUID, AtomicInteger> busBatchSizes,
            ConcurrentHashMap<UUID, Long> busLastFlushTime,
            ConcurrentHashMap<UUID, Trip> busTripCache
    ) {
        long now = System.currentTimeMillis();

        for (UUID busId : busQueues.keySet()) {
            Long lastFlush = busLastFlushTime.get(busId);
            if (lastFlush != null) {
                long ageSeconds = (now - lastFlush) / 1000;
                if (ageSeconds >= timeoutSeconds) {
                    Trip trip = busTripCache.get(busId);

                    if(busBatchSizes.containsKey(busId))
                        if(busBatchSizes.get(busId).get() > 0)
                            flushBusBatch(busId, busQueues,
                                    busBatchSizes, busLastFlushTime,
                                    trip, "timeout (" + ageSeconds + " seconds old)"
                            );
                }
            }
        }
    }


    public void flushBusBatch(
            UUID busId,
            ConcurrentHashMap<UUID, ConcurrentLinkedQueue<BusCurrentLocationMessage>> busQueues,
            ConcurrentHashMap<UUID, AtomicInteger> busBatchSizes,
            ConcurrentHashMap<UUID, Long> busLastFlushTime,
            Trip trip,
            String reason
    ) {
        var queue = busQueues.get(busId);
        var sizeCounter = busBatchSizes.get(busId);

        if (queue == null || queue.isEmpty()) {
            return;
        }

        //removing everything from the que
        List<BusCurrentLocationMessage> locationsToSave = new ArrayList<>();
        BusCurrentLocationMessage location;
        while ((location = queue.poll()) != null)
            locationsToSave.add(location);


        int drainedCount = locationsToSave.size();

        if (drainedCount == 0)
            return;


        // Update counter
        if (sizeCounter != null)
            sizeCounter.addAndGet(-drainedCount);

        // Update last flush time
        busLastFlushTime.put(busId, System.currentTimeMillis());

        log.info("Bus {}: Saving {} locations (Reason: {})", busId, drainedCount, reason);

        List<TripHistoryPoint> entities = this.busTrackingMapper.toEntities(locationsToSave, trip);
        this.tripHistoryRepository.saveAll(entities);
        log.info("Bus {}: Batch saved successfully", busId);
    }
}