package org.tracker.ubus.ubus.Components.BusTracking.Mappers;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.BusTracking.DTO.Internal.BusLocationsCarrierInternal;
import org.tracker.ubus.ubus.Components.BusTracking.DTO.Requests.BusCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.TripHistory.Entity.TripHistoryPoint;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BusTrackingMapper {


    public TripHistoryPoint toEntity(BusCurrentLocationMessage location, Trip trip) {
        return TripHistoryPoint.builder()
                .latitude(location.latitude())
                .longitude(location.longitude())
                .speed(location.speed())
                .trip(trip)
                .timePosted(location.timePosted())
                .build();

    }

    public List<TripHistoryPoint> toEntities(List<BusCurrentLocationMessage> locations, Trip trip) {
        return locations.stream()
                .map(locationMessage -> toEntity(locationMessage, trip))
                .toList();
    }

    public BusLocationsCarrierInternal toInternal(
            ConcurrentLinkedQueue<BusCurrentLocationMessage> busCurrentLocationMessages,
            ConcurrentMap<UUID, ConcurrentLinkedQueue<BusCurrentLocationMessage>> allCurrentBusLocations,
            ConcurrentMap<UUID, Long> busLastFlush,
            ConcurrentMap<UUID, AtomicInteger> tripBatchSize,
            Trip trip) {

        return BusLocationsCarrierInternal.builder()
                .busCurrentLocationMessages(busCurrentLocationMessages)
                .allCurrentBusLocations(allCurrentBusLocations)
                .busLastFlush(busLastFlush)
                .tripBatchSize(tripBatchSize)
                .trip(trip)
                .build();
    }

}
