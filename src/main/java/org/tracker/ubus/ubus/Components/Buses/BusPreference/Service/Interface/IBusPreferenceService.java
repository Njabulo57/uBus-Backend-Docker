package org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Interface;

import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Request.BusPreferenceDTO;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPrefView;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceResponse;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

import java.util.List;
import java.util.Map;

public interface IBusPreferenceService {
    public void addPreference(List<BusPreferenceDTO> busPreferenceDTO);
    public void editPreference(BusPreferenceDTO busPreferenceDTO);
    public void deletePreference(BusPreferenceDTO busPreferenceDTO);
    public BusPreferenceResponse viewPreferences();


    BusPrefView[] getAllBusPreferences();

    boolean hasBusPreference();
}
