package org.tracker.ubus.ubus.Components.Buses.BusTracking.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
public class BusTrackingConfiguration {

    /**
     * Creates a thread-safe map that holds a location message queue for each active bus.
     * This bean is the central buffer for incoming driver location updates before they are
     * batched and persisted to the database. Every time a driver sends a location update
     * during an active trip, the message is added to their bus's dedicated queue.
     * Key: Bus ID (UUID) Value: BlockingQueue of DriverCurrentLocationMessage
     * Each bus gets its own queue, providing isolation between buses.
     * Queue is bounded to prevent memory exhaustion (max size defined in service).
     * When queue is full, the oldest message is dropped and newest is added.
     * Messages are drained in batches and saved to the trip_history table.
     *
     * @return a new ConcurrentHashMap that maps bus UUID to its blocking queue of location messages
     */
    @Bean
    public ConcurrentHashMap<UUID, BlockingQueue<DriverCurrentLocationMessage>> busQueues() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Tracks whether a bus is currently being flushed to prevent concurrent flush operations.
     *
     * @return ConcurrentHashMap mapping bus ID to an atomic boolean flush flag
     */
    @Bean
    public ConcurrentHashMap<UUID, AtomicBoolean> busLastFlush() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Caches the active trip for each bus to avoid database lookups on every location update.
     *
     * @return ConcurrentHashMap mapping bus ID to active trip entity
     */
    @Bean
    public ConcurrentHashMap<UUID, Trip> busTripCache() {
        return new ConcurrentHashMap<>();
    }
}