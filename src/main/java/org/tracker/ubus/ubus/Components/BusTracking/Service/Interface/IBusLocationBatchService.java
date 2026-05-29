package org.tracker.ubus.ubus.Components.BusTracking.Service.Interface;

import org.tracker.ubus.ubus.Components.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.BusTracking.DTO.Responses.DriverCurrentLocationResponse;

import java.util.UUID;

public interface IBusLocationBatchService {



    DriverCurrentLocationResponse enqueue(DriverCurrentLocationMessage location);

    void  endTrip(UUID tripId);
}
