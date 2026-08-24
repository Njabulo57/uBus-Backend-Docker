package org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Interface;

import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Request.BusPreferenceDTO;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPrefView;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceResponse;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IBusPreferenceService {
    void addPreference(List<BusPreferenceDTO> busPreferenceDTO);
    void editPreference(BusPreferenceDTO busPreferenceDTO);
    void deletePreference(BusPreferenceDTO busPreferenceDTO);
    BusPreferenceResponse viewPreferences();


    Collection<BusPrefView> getAllBusPreferences();

    boolean hasBusPreference();
}
