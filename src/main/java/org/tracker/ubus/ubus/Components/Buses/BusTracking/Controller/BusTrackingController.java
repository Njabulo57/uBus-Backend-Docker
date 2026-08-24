package org.tracker.ubus.ubus.Components.Buses.BusTracking.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Service.Interface.IBusLocationTrackingService;

/**
 * The BusTrackingController class is responsible for handling real-time bus tracking operations
 * such as managing updates on bus locations and ending bus trips.
 * This controller communicates over WebSocket channels to notify clients about bus location updates.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class BusTrackingController {

    private final IBusLocationTrackingService busTrackingService;

    @MessageMapping("/buses/getFromTripSimulationCache-new-route")
    public void busTracking(@Payload DriverCurrentLocationMessage location) {
        this.busTrackingService.enqueue(location);
    }

}
