package org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;


@Getter
public class TripUserOnTappedOutCardEvent extends ApplicationEvent {

    private final String jwtToken;
    private final Trip trip;

    public TripUserOnTappedOutCardEvent(Object source, String jwtToken, Trip trip) {
        super(source);
        this.jwtToken = jwtToken;
        this.trip = trip;
    }
}
