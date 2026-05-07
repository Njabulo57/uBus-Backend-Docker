package org.tracker.ubus.ubus.Components.Bus.Service.Interface;


import org.tracker.ubus.ubus.Components.Bus.DTOs.Requests.BusRegisterRequest;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusAdminViewWrapperResponse;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusRegisterResponse;

public interface IBusService {

    BusRegisterResponse registerBus(BusRegisterRequest request);


    BusAdminViewWrapperResponse viewBuses();
}
