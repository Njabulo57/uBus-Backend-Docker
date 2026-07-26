package org.tracker.ubus.ubus.Components.Buses.BusTracking.Service.Interface;

import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverRouteChange;

import java.util.UUID;

public interface IBusRouteChangeService {


    Object changeRoute(DriverRouteChange driverRouteChange);
}
