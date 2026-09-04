package org.tracker.ubus.ubus.Components.Trips.TripTraffic.TrafficLookUp;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.DTO.Internal.TrafficInfoCarrier;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.Entity.Traffic;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TrafficLookUp {

    private final Cache<UUID, TrafficInfoCarrier> trafficCache;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:a");


    public void addTrafficInfoToCache(UUID tripId, Traffic traffic) {
        var reason = traffic.getTrafficType().getLabel();
        var delay = traffic.getDelayMinutes();
        var trafficInfoCarrier = TrafficInfoCarrier.of(reason, delay);
        this.trafficCache.put(tripId, trafficInfoCarrier);
    }

    public TrafficInfoCarrier getTrafficInfoFromCache(UUID tripId) {
        return this.trafficCache.getIfPresent(tripId);
    }


    public DelayStatus getDelayStatusFromTrafficIfExists(Trip trip) {
        var trafficCacheEntry = this.trafficCache.getIfPresent(trip.getId());
        if(trafficCacheEntry != null) {

            var reasonLate = trafficCacheEntry.reason();
            var delayInMinutes = trafficCacheEntry.delayInMinutes();
            var eta = LocalTime.now().plusMinutes(delayInMinutes);
            var formattedEta = formatter.format(eta);
            return new DelayStatus(true, delayInMinutes, eta, formattedEta, reasonLate);
        }
        return null;
    }


}

