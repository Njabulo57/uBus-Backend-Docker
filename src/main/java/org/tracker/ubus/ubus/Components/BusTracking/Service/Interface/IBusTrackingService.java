package org.tracker.ubus.ubus.Components.BusTracking.Service.Interface;

import org.tracker.ubus.ubus.Components.BusTracking.DTO.Requests.BusCurrentLocationMessage;
import java.util.UUID;


public interface IBusTrackingService {

    UUID queueLocationForItsBatch(BusCurrentLocationMessage location);
}
