package org.tracker.ubus.ubus.Components.Buses.BusTracking.Service.Interface;

import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Responses.DriverCurrentLocationResponse;

import java.util.UUID;

public interface IBusLocationBatchService {



    DriverCurrentLocationResponse enqueue(DriverCurrentLocationMessage location);

    void  endTrip(UUID tripId);
}
