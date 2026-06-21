package org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Interface;

import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

public interface IBusPreferenceService {
    public BusPreferenceResponse addPreference(Route route);
    public BusPreferenceResponse editPreference(Route oldRoute,Route newRoute);
    public void deletePreference(Route oldRoute);
    public BusPreferenceResponse viewPreferences();
}
