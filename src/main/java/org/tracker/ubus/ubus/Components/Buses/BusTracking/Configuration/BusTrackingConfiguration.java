package org.tracker.ubus.ubus.Components.Buses.BusTracking.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Configuration
public class BusTrackingConfiguration {

    /**
     * Creates a thread-safe map that holds a location message queue for each active bus.
     * This bean is the central buffer for incoming driver location updates before they are
     * batched and persisted to the database. Every time a driver sends a location update
     * during an active trip, the message is added to their bus's dedicated queue.
     * Key: tripID (UUID) Value: BlockingQueue of DriverCurrentLocationMessage
     * Each bus gets its own queue, providing isolation between buses.
     * Queue is bounded to prevent memory exhaustion (max size defined in service).
     * When queue is full, the oldest message is dropped and newest is added.
     * Messages are drained in batches and saved to the trip_history table.
     *
     * @return a new ConcurrentHashMap that maps bus UUID to its blocking queue of location messages
     */
    @Bean
    public ConcurrentHashMap<UUID, ConcurrentLinkedDeque<DriverCurrentLocationMessage>> busQueues() {
        return new ConcurrentHashMap<>();
    }


    /**
     * Creates a thread-safe set to track UUIDs of buses currently being monitored for proximity.
     * This set is used to manage buses that are actively monitored for proximity
     * calculations, such as determining nearby buses or geographic clustering.
     *
     * @return a thread-safe set of UUIDs representing the buses being monitored for proximity
     */
    @Bean
    Set<UUID> busMonitoringForProximity() {
        return ConcurrentHashMap.newKeySet();
    }


    /**
     * Creates a thread-safe map for storing the route coordinates of buses.
     * This map associates each bus, identified by a unique UUID, with an array of double values
     * representing its geographic route coordinates.
     *
     * @return a new ConcurrentHashMap where the key is the UUID of a trip
     *         and the value is an array of doubles representing the bus's route coordinates.
     */
    @Bean
    public ConcurrentHashMap<UUID, double[]> busRouteCoordinates() {
        return new ConcurrentHashMap<>();
    }


    /**
     * Creates a thread-safe map that holds the details of all active trips.
     * This map associates each trip with its corresponding {@link Trip} object,
     * identified by a unique UUID. The map is designed to allow concurrent access,
     * ensuring thread safety in a multi-threaded environment.
     *
     * @return a new ConcurrentHashMap where the key is a UUID representing the trip ID
     *         and the value is a*/
    @Bean
    public ConcurrentHashMap<UUID, Trip> tripCacheMap() {
        return new ConcurrentHashMap<>();
    }
}