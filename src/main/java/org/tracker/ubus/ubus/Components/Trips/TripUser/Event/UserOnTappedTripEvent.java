package org.tracker.ubus.ubus.Components.Trips.TripUser.Event;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

import java.time.LocalTime;


@Getter
public class UserOnTappedTripEvent extends ApplicationEvent {

    private final boolean isCompleted;
    private final String proximityMessage;


    private final Trip trip;

    private final Destination to;
    private final Destination from;
    private final DelayStatus delayStatus;
    private final LocalTime departureTime;


    public UserOnTappedTripEvent(Object source, String proximityMessage, boolean isCompleted, Trip trip,
                                 Destination to, Destination from,
                                 DelayStatus delayStatus, LocalTime departureTime) {
        super(source);
        this.proximityMessage = proximityMessage;
        this.isCompleted = isCompleted;
        this.trip = trip;
        this.to = to;
        this.from = from;
        this.delayStatus = delayStatus;
        this.departureTime = departureTime;
    }
}
