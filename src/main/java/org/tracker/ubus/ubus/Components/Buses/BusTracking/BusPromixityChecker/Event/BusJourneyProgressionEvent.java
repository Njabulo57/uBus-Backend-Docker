package org.tracker.ubus.ubus.Components.Buses.BusTracking.BusPromixityChecker.Event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.BusPromixityChecker.DTO.BusJourneyTripProgression;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;

import java.util.Collection;
import java.util.Map;

@Getter
public class BusJourneyProgressionEvent extends ApplicationEvent {

    private final String message;
    private final Collection<BusJourneyTripProgression> tripProgressions;

    public BusJourneyProgressionEvent(Object source, Collection<BusJourneyTripProgression> tripProgressions, String message) {
        super(source);
        this.message = message;
        this.tripProgressions = tripProgressions;
    }
}
