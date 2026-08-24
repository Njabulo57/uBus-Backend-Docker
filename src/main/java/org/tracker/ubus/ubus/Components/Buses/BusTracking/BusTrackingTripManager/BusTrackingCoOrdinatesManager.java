package org.tracker.ubus.ubus.Components.Buses.BusTracking.BusTrackingTripManager;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import java.util.UUID;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class BusTrackingCoOrdinatesManager {

    private final ConcurrentMap<UUID, ConcurrentLinkedDeque<DriverCurrentLocationMessage>> allCurrentBusLocations;


    public void addFirstLocation(double latitude, double longitude, UUID tripId) {
        var deq = new ConcurrentLinkedDeque<DriverCurrentLocationMessage>();
        var location = DriverCurrentLocationMessage.of(latitude, longitude);
        deq.offer(location);
        this.allCurrentBusLocations.put(tripId, deq);
    }


    public void removeTrip(UUID tripId) {

        var deq = this.allCurrentBusLocations.get(tripId);
        if(deq == null)
            return;
        deq.clear();
        this.allCurrentBusLocations.remove(tripId);
    }
}
