package org.tracker.ubus.ubus.Components.Buses.BusTracking.BusPromixityChecker.Event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;

import java.util.Collection;

@Getter
public class BusArrivedAtDestinationEvent extends ApplicationEvent {

    private final Collection<Trip> trips;

    public BusArrivedAtDestinationEvent(Object source, Collection<Trip> trips) {
        super(source);
        this.trips = trips;
    }
}
