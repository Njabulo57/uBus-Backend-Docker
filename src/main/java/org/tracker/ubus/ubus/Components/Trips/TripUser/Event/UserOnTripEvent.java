package org.tracker.ubus.ubus.Components.Trips.TripUser.Event;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

import java.time.LocalTime;


@Getter
public class UserOnTripEvent extends ApplicationEvent {

    private final boolean isCompleted;
    private final String proximityMessage;


    private final Trip trip;
    private final DelayStatus delayStatus;

    private final double speed;
    private final int progressBar;
    private final double distance;


    public UserOnTripEvent(Object source, String proximityMessage,
                           boolean isCompleted, Trip trip,
                           DelayStatus delayStatus, double speed, int progressBar, double distance) {
        super(source);
        this.proximityMessage = proximityMessage;
        this.isCompleted = isCompleted;
        this.trip = trip;
        this.speed = speed;
        this.progressBar = progressBar;
        this.distance = distance;
        this.delayStatus = delayStatus;
    }
}
