package org.tracker.ubus.ubus.Components.Users.Driver.Service.Interface;

import org.tracker.ubus.ubus.Components.Users.Driver.DTO.Response.BusAssignedResponse;

public interface IDriverService {

    boolean hasAssignedBus();


    BusAssignedResponse getDriverAssignedBus();
}
