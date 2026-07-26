package org.tracker.ubus.ubus.Components.Trips.Trip.Events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;

@Getter
public class TripEtaUpdateEvent extends ApplicationEvent {

    private final Trip trip;
    private final DelayStatus delayStatus;


    public TripEtaUpdateEvent(Object source, Trip trip, DelayStatus delayStatus) {
        super(source);
        this.trip = trip;
        this.delayStatus = delayStatus;
    }

}
