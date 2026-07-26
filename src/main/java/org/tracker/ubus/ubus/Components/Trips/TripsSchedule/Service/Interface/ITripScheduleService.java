package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Service.Interface;



import java.util.Collection;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses.DriverTodayScheduleResponse;

public interface ITripScheduleService {


    Collection<DriverTodayScheduleResponse> getDriverTodaySchedule();

    DriverTodayScheduleResponse getCurrentDriverScheduleTrip();

    DriverTodayScheduleResponse getNextDriverScheduleTrip();

}
