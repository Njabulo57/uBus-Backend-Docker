package org.tracker.ubus.ubus.Components.Trips.Trip.Events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;

@Getter
public class TripUserOnTappedCardEvent extends ApplicationEvent {

    private final Trip trip;
    private final String jwtToken;

    public TripUserOnTappedCardEvent(Object source, String jwtToken, Trip trip) {
        super(source);
        this.trip = trip;
        this.jwtToken = jwtToken;
    }
}
