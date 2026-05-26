package org.tracker.ubus.ubus.Components.BusTracking.Service.Interface;

import org.tracker.ubus.ubus.Components.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.BusTracking.DTO.Responses.DriverCurrentLocationResponse;



@FunctionalInterface
public interface IBusTrackingService {

    DriverCurrentLocationResponse queueLocationForItsBatch(DriverCurrentLocationMessage location);
}
