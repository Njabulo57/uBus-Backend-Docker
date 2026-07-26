package org.tracker.ubus.ubus.Components.Buses.BusTracking.Event.Socket;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;

import java.util.Collection;

@Getter
public class BusRouteChangeEvent extends ApplicationEvent {

    private final Collection<double[]> coordinates;
    private final Trip trip;

    public BusRouteChangeEvent(Object source, Collection<double[]> coordinates, Trip trip) {
        super(source);
        this.coordinates = coordinates;
        this.trip = trip;
    }
}
