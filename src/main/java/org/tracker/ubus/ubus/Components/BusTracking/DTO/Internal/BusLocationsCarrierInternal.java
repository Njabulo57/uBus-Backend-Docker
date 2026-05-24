package org.tracker.ubus.ubus.Components.BusTracking.DTO.Internal;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.BusTracking.DTO.Requests.BusCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Trip.Entity.Trip;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Builder
public record BusLocationsCarrierInternal(
        ConcurrentLinkedQueue<BusCurrentLocationMessage> busCurrentLocationMessages,
        ConcurrentMap<UUID, ConcurrentLinkedQueue<BusCurrentLocationMessage>> allCurrentBusLocations,
        ConcurrentMap<UUID, Long> busLastFlush,
        ConcurrentMap<UUID, AtomicInteger> tripBatchSize,
        Trip trip
) {
}
