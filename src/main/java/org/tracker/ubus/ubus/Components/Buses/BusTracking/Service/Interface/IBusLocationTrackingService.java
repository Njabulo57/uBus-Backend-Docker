package org.tracker.ubus.ubus.Components.Buses.BusTracking.Service.Interface;

import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;

public interface IBusLocationTrackingService {

   void enqueue(DriverCurrentLocationMessage location);

}
