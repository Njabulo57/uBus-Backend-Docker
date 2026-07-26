package org.tracker.ubus.ubus.Components.Buses.BusPreference.Events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Internal.ClosestTripInfo;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceClosestTripResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.UserPreferenceNearestBus;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

@Getter
public class BusPreferenceSuscriberEvent extends ApplicationEvent {


    private final User user;
    private final UserPreferenceNearestBus nearestBusByPref;

    public BusPreferenceSuscriberEvent(Object source, User user, UserPreferenceNearestBus nearestBusByPref) {
        super(source);
        this.user = user;
        this.nearestBusByPref = nearestBusByPref;
    }
}
