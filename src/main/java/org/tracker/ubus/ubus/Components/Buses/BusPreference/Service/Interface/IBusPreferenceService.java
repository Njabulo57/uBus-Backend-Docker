package org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Interface;

import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

public interface IBusPreferenceService {
    public BusPreference addPreference(Route route);
    public BusPreference editPreference(Route oldRoute,Route newRoute);
    public void deletePreference(Route oldRoute);
}
