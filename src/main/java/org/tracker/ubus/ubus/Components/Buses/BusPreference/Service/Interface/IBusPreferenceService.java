package org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Interface;

import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Request.BusPreferenceDTO;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

public interface IBusPreferenceService {
    public BusPreferenceResponse addPreference(BusPreferenceDTO busPreferenceDTO);
    public BusPreferenceResponse editPreference(BusPreferenceDTO busPreferenceDTO);
    public void deletePreference(BusPreferenceDTO busPreferenceDTO);
    public BusPreferenceResponse viewPreferences();
}
