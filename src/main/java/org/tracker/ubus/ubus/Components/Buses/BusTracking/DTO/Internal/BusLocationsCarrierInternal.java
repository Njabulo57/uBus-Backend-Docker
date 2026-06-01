package org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Builder
public record BusLocationsCarrierInternal(
        ConcurrentLinkedQueue<DriverCurrentLocationMessage> driverCurrentLocationMessages,
        ConcurrentMap<UUID, ConcurrentLinkedQueue<DriverCurrentLocationMessage>> allCurrentBusLocations,
        ConcurrentMap<UUID, Long> busLastFlush,
        ConcurrentMap<UUID, AtomicInteger> tripBatchSize,
        Trip trip
) {
}
