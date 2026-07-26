package org.tracker.ubus.ubus.Components.Buses.BusPreference.Events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.Collection;
import java.util.Set;

@Getter
public class BusPreferenceSuscriberAllTripsEvent extends ApplicationEvent {

    private final Trip trip;
    private final String eta;
    private final Collection<User> users;
    private final String distanceInKM;
    private final DelayStatus delayStatus;


    public BusPreferenceSuscriberAllTripsEvent(Object source, Collection<User> users, Trip trip, String eta, String distanceInKM, DelayStatus delayStatus) {
        super(source);
        this.users = users;
        this.trip = trip;
        this.eta = eta;
        this.distanceInKM = distanceInKM;
        this.delayStatus = delayStatus;
    }
}
