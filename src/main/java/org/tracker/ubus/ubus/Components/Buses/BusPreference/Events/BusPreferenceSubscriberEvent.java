package org.tracker.ubus.ubus.Components.Buses.BusPreference.Events;

import lombok.Getter;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.UserPreferenceNearestBus;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

@Getter
public class BusPreferenceSubscriberEvent extends BusSubscriptionEvent {


    private final User user;
    private final UserPreferenceNearestBus nearestBusByPref;

    public BusPreferenceSubscriberEvent(Object source, User user,
                                        UserPreferenceNearestBus nearestBusByPref, int progress) {
        super(source, progress);
        this.user = user;
        this.nearestBusByPref = nearestBusByPref;
    }
}
