package org.tracker.ubus.ubus.Components.Buses.BusTracking.Event.Socket;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Responses.DriverCurrentLocationResponse;
import org.tracker.ubus.ubus.Components.Trips.Trip.Service.Impl.TripService;


@Getter
public class BusTrackingLocationDeliveryEvent extends ApplicationEvent {

    private final DriverCurrentLocationResponse[] locations;

    public BusTrackingLocationDeliveryEvent(Object source, DriverCurrentLocationResponse... locations) {
        super(source);
        this.locations = locations;
    }

    public BusTrackingLocationDeliveryEvent(Object source, DriverCurrentLocationResponse location) {
        super(source);
        this.locations = new DriverCurrentLocationResponse[]{location};
    }

}
