package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Service.Impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Service.Impl.TripService;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Internal.ScheduleQueryContext;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses.DriverTodayScheduleResponse;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses.ScheduleResponse;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses.ToDestinationResponse;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Mapper.ScheduleMapper;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Service.Interface.ITripScheduleService;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Util.TripScheduleUtil;
import java.time.LocalDate;
import java.util.Collection;

@Service
@RequiredArgsConstructor
class TripScheduleService extends BaseService implements ITripScheduleService {

    private final ScheduleMapper shScheduleMapper;
    private final BusAssignmentRepository busAssignmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final TripService tripService;


    @Override
    public Collection<DriverTodayScheduleResponse> getDriverTodaySchedule() {

        final var queryContext = this.getQueryContext();
        return TripScheduleUtil.getWithShceduleContext(context -> {
                       var result = this.scheduleRepository.findByBusAndServiceDateAndTimeRange(context.bus(), context.currentDate(),
                                        context.startTime(), context.endTime()
                                );
                       return this.shScheduleMapper.toDriverTodaySchedule(result);
                },
                queryContext
        );
    }


    @Override
    public DriverTodayScheduleResponse getCurrentDriverScheduleTrip() {

        final var queryContext = this.getQueryContext();
        return TripScheduleUtil.getWithShceduleContext(context ->
                        this.scheduleRepository.findDriverCurrentScheduleTrip(context.bus(), context.currentDate(),
                        context.startTime(), context.endTime()
                ).map(this.shScheduleMapper::toDriverTodaySchedule)
                .orElse( DriverTodayScheduleResponse.builder()
                        .build()
                ),
                queryContext
        );
    }

    @Override
    public DriverTodayScheduleResponse getNextDriverScheduleTrip() {
        final var queryContext = this.getQueryContext();
        return TripScheduleUtil.getWithShceduleContext(context -> {
            var schedules = this.scheduleRepository.findDriverNextScheduleTrip(
                    context.bus(),
                    context.currentDate(),
                    context.startTime(),
                    context.endTime()
            );

            var next = schedules.size() >= 2 ? schedules.get(1)
                    : (schedules.size() == 1 ? schedules.get(0) : null);

            return next != null ? this.shScheduleMapper.toDriverTodaySchedule(next)
                    : DriverTodayScheduleResponse.builder().build();
        }, queryContext);
    }


    private ScheduleQueryContext getQueryContext() {
        var user = getCurrentUser();
        var busAssignment = this.busAssignmentRepository.findByDriverOrThrow(user);
        var driverSchedule = busAssignment.getDriverSchedule();

        var bus = busAssignment.getBus();
        var startTime = driverSchedule.getStartTime();
        var endTime = driverSchedule.getEndTime();

        return ScheduleQueryContext.builder()
                .bus(bus)
                .startTime(startTime).endTime(endTime)
                .currentDate(LocalDate.now())
                .build();
    }

}
