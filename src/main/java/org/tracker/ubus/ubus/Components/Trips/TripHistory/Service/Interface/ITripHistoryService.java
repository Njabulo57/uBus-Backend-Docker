package org.tracker.ubus.ubus.Components.Trips.TripHistory.Service.Interface;


import org.springframework.data.domain.Pageable;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response.PastTripsPageResponse;

public interface ITripHistoryService {



    Object getPastTrips(Pageable pageable);
}
