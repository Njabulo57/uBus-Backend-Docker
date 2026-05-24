package org.tracker.ubus.ubus.Components.Bus.Service.Interface;


import org.tracker.ubus.ubus.Components.Bus.DTOs.Requests.BusEditRequest;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Requests.BusRegisterRequest;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusAdminViewResponse;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusAdminViewWrapperResponse;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusRegisterResponse;

import java.util.List;
import java.util.UUID;

public interface IBusService {

    BusRegisterResponse registerBus(BusRegisterRequest request);


    void editBus(BusEditRequest request, UUID busId);

    List<BusAdminViewResponse> viewBuses();
}
