package org.tracker.ubus.ubus.Components.Trips.TripLate.Service.Interface;

import org.tracker.ubus.ubus.Components.Trips.TripLate.DTO.Request.TripLateRequest;

public interface ITripLateService {
    void updateTripLate(TripLateRequest tripLateRequest);
}
