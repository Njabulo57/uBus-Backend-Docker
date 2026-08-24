package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Service.Interface;



import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Internal.LocationCarrier;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Internal.TimeRangeCarrier;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Requests.ScheduleLegRequest;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Requests.ScheduleRequest;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Requests.ScheduleScheduleLegRequest;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses.*;

public interface ITripScheduleService {

    Collection<ScheduleBaseInformation> getScheduleBaseInformation();


    Collection<ScheduleRouteWrapperResponse> getScheduleRouteWrapperResponse(String route);

    Collection<DriverTodayScheduleResponse> getDriverTodaySchedule();

    DriverTodayScheduleResponse getCurrentDriverScheduleTrip();

    DriverTodayScheduleResponse getNextDriverScheduleTrip();

    ScheduleObjectResponse addSchedule(ScheduleRequest scheduleRequest);
    void removeSchedule(ScheduleRequest scheduleRequest);
    ScheduleObjectResponse updateSchedule(ScheduleRequest scheduleRequest);

    ScheduleLegResponse addScheduleLeg(ScheduleLegRequest scheduleLegRequest);
    void removeScheduleLeg(ScheduleLegRequest scheduleLegRequest);
    ScheduleLegResponse updateScheduleLeg(ScheduleLegRequest scheduleLegRequest);
    List<ScheduleObjectResponse> getSchedules();
    List<ScheduleLegResponse> getScheduleLegs();
    ScheduleScheduleLegResponse getScheduleScheduleLeg(ScheduleScheduleLegRequest scheduleScheduleLegRequest);
    ScheduleScheduleLegResponse addScheduleScheduleLeg(ScheduleScheduleLegRequest scheduleScheduleLegRequest);
    ScheduleScheduleLegResponse updateScheduleScheduleLeg(ScheduleScheduleLegRequest scheduleScheduleLegRequest);

    ScheduleViewWrapper getScheduleView(Pageable pageable, LocationCarrier locationCarrier, TimeRangeCarrier timeRangeCarrier);
}
