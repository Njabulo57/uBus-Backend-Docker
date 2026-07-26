package org.tracker.ubus.ubus.Components.Trips.Trip.Events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus;

import java.util.UUID;

@Getter
public class TripBusActivityStatusChangeEvent extends ApplicationEvent {

    private final UUID tripId;
    private final BusActivityStatus busActivityStatus;

    public TripBusActivityStatusChangeEvent(Object source, UUID tripId, BusActivityStatus busActivityStatus) {
        super(source);
        this.tripId = tripId;
        this.busActivityStatus = busActivityStatus;
    }

}
