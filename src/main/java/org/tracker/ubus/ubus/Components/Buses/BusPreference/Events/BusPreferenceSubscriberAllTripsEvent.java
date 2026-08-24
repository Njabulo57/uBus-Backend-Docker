package org.tracker.ubus.ubus.Components.Buses.BusPreference.Events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.Collection;
import java.util.Set;

@Getter
public class BusPreferenceSubscriberAllTripsEvent extends BusSubscriptionEvent{

    private final Trip trip;
    private final String eta;
    private final String distanceInKM;
    private final Collection<User> users;
    private final DelayStatus delayStatus;


    public BusPreferenceSubscriberAllTripsEvent(Object source, Collection<User> users, Trip trip, String eta,
                                                String distanceInKM, DelayStatus delayStatus, int progress) {
        super(source, progress);
        this.eta = eta;
        this.trip = trip;
        this.users = users;
        this.delayStatus = delayStatus;
        this.distanceInKM = distanceInKM;

    }
}
