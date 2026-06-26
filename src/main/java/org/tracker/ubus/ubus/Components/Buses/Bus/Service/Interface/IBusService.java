package org.tracker.ubus.ubus.Components.Buses.Bus.Service.Interface;


import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Requests.BusEditRequest;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Requests.BusRegisterRequest;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Responses.BusAdminViewResponse;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Responses.BusRegisterResponse;

import java.util.List;
import java.util.UUID;

public interface IBusService {

    BusRegisterResponse registerBus(BusRegisterRequest request);

    void editBusActivityStatus(String busId, String activityStatus);

    void deleteBus(UUID busId);

    void editBus(BusEditRequest request);

    List<BusAdminViewResponse> viewBuses();
}
