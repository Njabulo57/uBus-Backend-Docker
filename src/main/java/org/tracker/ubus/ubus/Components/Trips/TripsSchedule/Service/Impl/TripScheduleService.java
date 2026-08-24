package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Service.Impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Internal.LocationCarrier;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Internal.ScheduleQueryContext;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Internal.TimeRangeCarrier;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Requests.ScheduleLegRequest;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Requests.ScheduleRequest;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Requests.ScheduleScheduleLegRequest;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses.*;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleDatesExcluded;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLeg;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLegBusAssignment;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Mapper.ScheduleMapper;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleLegAssignmentRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleLegRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Service.Interface.ITripScheduleService;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Util.TripScheduleUtil;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;


@Service
@RequiredArgsConstructor
class TripScheduleService extends BaseService implements ITripScheduleService {

    private final TripRepository tripRepository;
    private final ScheduleMapper shScheduleMapper;
    private final BusAssignmentRepository busAssignmentRepository;

    private final ScheduleLegAssignmentRepository scheduleLegAssignmentRepository;
    private final ScheduleLegRepository scheduleLegRepository;
    private final ScheduleRepository scheduleRepository;
    private final EntityManager entityManager;


    @Override
    public Collection<DriverTodayScheduleResponse> getDriverTodaySchedule() {
        final var queryContext = this.getQueryContext();

        return TripScheduleUtil.getWithShceduleContext(context -> {
            var bus = context.bus();

            var today = context.currentDate();

            var dayOfWeek =today.getDayOfWeek();
            var todaySchedule = this.scheduleLegAssignmentRepository.findDriverScheduleTripsForToday(bus, today,
                    dayOfWeek);

            todaySchedule = getUniqueLegs(todaySchedule);

            var completeStatus = TripStatus.COMPLETE;
            var tripsTodayByDriver = this.tripRepository.findAllTripsByBusAssignmentForToday(bus, today,
                    completeStatus);

            return this.shScheduleMapper.toDriverTodaySchedule(todaySchedule, tripsTodayByDriver);
        }, queryContext);
    }


    @Override
    public Collection<ScheduleBaseInformation> getScheduleBaseInformation() {
       return Collections.emptyList();
    }


    @Override
    public Collection<ScheduleRouteWrapperResponse> getScheduleRouteWrapperResponse(String route) {
       return Collections.emptyList();
    }


    @Override
    public DriverTodayScheduleResponse getCurrentDriverScheduleTrip() {
        final var queryContext = this.getQueryContext();

        return TripScheduleUtil.getWithShceduleContext(context -> {

            var bus = context.bus();
            var today = context.currentDate();
            var dayOfWeek =  today.getDayOfWeek();
            // Get all schedule legs for today
            var scheduleLegs = this.scheduleLegAssignmentRepository.findDriverScheduleTripsForToday(bus, today,
                            dayOfWeek);
            scheduleLegs = getUniqueLegs(scheduleLegs);
            // Get all trips done by this bu assignment for today
            var completeStatus = TripStatus.COMPLETE;
            var trips = this.tripRepository
                    .findAllTripsByBusAssignmentForToday(bus, today, completeStatus);

            // Find the first schedule leg that doesn't have a completed trip yet
            var currentScheduleLeg = scheduleLegs.stream()
                    .filter(sla -> {
                        boolean hasCompletedTrip = trips.stream()
                                .anyMatch(trip -> trip.getScheduleLegBusAssignment().getId().
                                        equals(sla.getId())
                                        && trip.getStatus() == TripStatus.COMPLETE);
                        return !hasCompletedTrip;
                    })
                    .findFirst()
                    .orElse(null);

            if (currentScheduleLeg == null)
                return DriverTodayScheduleResponse.builder()
                        .build();

            return this.shScheduleMapper.toDriverTodaySchedule(currentScheduleLeg,
                    trips);

        }, queryContext);
    }


    @Override
    public DriverTodayScheduleResponse getNextDriverScheduleTrip() {
        final var queryContext = this.getQueryContext();

        var result = TripScheduleUtil.getWithShceduleContext(context -> {

            var bus = context.bus();

            var date = LocalDate.now();
            var dayOfWeek =  date.getDayOfWeek();

            // Get all schedule legs for today
            var scheduleLegs = this.scheduleLegAssignmentRepository
                    .findDriverScheduleTripsForToday(bus, date, dayOfWeek);
            scheduleLegs = getUniqueLegs(scheduleLegs);

            // Get all trips done by this bus assignment for today
            var completeStatus = TripStatus.COMPLETE;
            var trips = this.tripRepository
                    .findAllTripsByBusAssignmentForToday(bus, date, completeStatus);

            // Find the first schedule leg that doesn't have a completed trip yet (current)
            // Then get the next one after that
            var nextScheduleLeg = scheduleLegs.stream()
                    .filter(sla -> {
                        boolean hasCompletedTrip = trips.stream()
                                .anyMatch(trip -> trip.getScheduleLegBusAssignment().getId().equals(sla.getId())
                                        && trip.getStatus() == TripStatus.COMPLETE);
                        return !hasCompletedTrip;
                    })
                    .skip(1)  // Skip the current one to get the next
                    .findFirst()
                    .orElse(null);

            if (nextScheduleLeg == null)
                return DriverTodayScheduleResponse.builder()
                        .build();

            System.err.println("Found Something on leg to" + nextScheduleLeg.getScheduleLeg().getToDestination());
            return this.shScheduleMapper.toDriverTodaySchedule(nextScheduleLeg, trips);
        }, queryContext);

        return result;
    }


    @Override
    public ScheduleObjectResponse addSchedule(ScheduleRequest scheduleRequest) {

        Schedule schedule = Schedule.builder()
                .route(scheduleRequest.route())
                .validFromDate(scheduleRequest.validFromDate())
                .validToDate(scheduleRequest.validToDate())
                .build();
        scheduleRepository.save(schedule);
        return ScheduleObjectResponse.builder()
                .id(schedule.getId().toString())
                .route(schedule.getRoute().toString())
                .validFromDate(schedule.getValidFromDate().toString())
                .validToDate(schedule.getValidToDate().toString())
                .build();

    }



    @Override
    public void removeSchedule(ScheduleRequest scheduleRequest){
        scheduleRepository.deleteById(scheduleRequest.id());
    }


    @Override
    public ScheduleObjectResponse updateSchedule(ScheduleRequest scheduleRequest) {
        Schedule schedule = scheduleRepository.findByIdOrThrow(scheduleRequest.id());
        if(scheduleRequest.route()!= null)
            schedule.setRoute(scheduleRequest.route());
        if(scheduleRequest.validFromDate() != null)
            schedule.setValidFromDate(scheduleRequest.validFromDate());
        if(scheduleRequest.validToDate() != null)
            schedule.setValidToDate(scheduleRequest.validToDate());
        scheduleRepository.save(schedule);
        return ScheduleObjectResponse.builder()
                .id(schedule.getId().toString())
                .route(schedule.getRoute().toString())
                .validFromDate(schedule.getValidFromDate().toString())
                .validToDate(schedule.getValidToDate().toString())
                .build();
    }

    @Override
    public ScheduleLegResponse addScheduleLeg(ScheduleLegRequest scheduleLegRequest) {
        Schedule schedule = scheduleRepository.findByIdOrThrow(scheduleLegRequest.scheduleId());
        if(schedule.getScheduleLegs().stream().anyMatch(scheduleLeg -> scheduleLeg.getDepartureTime().equals(scheduleLegRequest.departureTime()) && scheduleLeg.getArrivalTime().equals(scheduleLegRequest.arrivalTime())
        && scheduleLeg.getDayOfWeek().equals(scheduleLegRequest.dayOfTheWeek()) && scheduleLeg.getFromDestination().equals(scheduleLegRequest.from())
                        && scheduleLeg.getToDestination().equals(scheduleLegRequest.to())))
            throw new IllegalArgumentException("ScheduleLeg Already Exists.");
        ScheduleLeg scheduleLeg = ScheduleLeg.builder()
                .schedule(schedule)
                .departureTime(scheduleLegRequest.departureTime())
                .arrivalTime(scheduleLegRequest.arrivalTime())
                .fromDestination(scheduleLegRequest.from())
                .toDestination(scheduleLegRequest.to())
                .dayOfWeek(scheduleLegRequest.dayOfTheWeek())
                .build();
        schedule.getScheduleLegs().add(scheduleLeg);

        entityManager.persist(scheduleLeg);

        return ScheduleLegResponse.builder()
                .id(scheduleLeg.getId().toString())
                .scheduleId(scheduleLegRequest.scheduleId().toString())
                .departureTime(scheduleLegRequest.departureTime().toString())
                .arrivalTime(scheduleLegRequest.arrivalTime().toString())
                .from(scheduleLegRequest.from().toString())
                .to(scheduleLegRequest.to().toString())
                .dayOfTheWeek(scheduleLegRequest.dayOfTheWeek().toString())
                .build();
    }

    @Override
    public void removeScheduleLeg(ScheduleLegRequest scheduleLegRequest) {
        ScheduleLeg scheduleLeg = scheduleRepository.findLegByIdOrThrow(scheduleLegRequest.id());
        Schedule schedule = scheduleRepository.findLegByIdOrThrow(scheduleLegRequest.scheduleId()).getSchedule();
        schedule.getScheduleLegs().remove(scheduleLeg);
        scheduleRepository.save(schedule);
    }

    @Override
    public ScheduleLegResponse updateScheduleLeg(ScheduleLegRequest scheduleLegRequest) {
        ScheduleLeg scheduleLeg = scheduleRepository.findLegByIdOrThrow(scheduleLegRequest.id());
        if(scheduleLegRequest.departureTime() != null)
            scheduleLeg.setDepartureTime(scheduleLegRequest.departureTime());
        if(scheduleLegRequest.arrivalTime() != null)
            scheduleLeg.setArrivalTime(scheduleLegRequest.arrivalTime());
        if(scheduleLegRequest.from() != null)
            scheduleLeg.setFromDestination(scheduleLegRequest.from());
        if(scheduleLegRequest.to() != null)
            scheduleLeg.setToDestination(scheduleLegRequest.to());
        if(scheduleLegRequest.dayOfTheWeek() != null)
            scheduleLeg.setDayOfWeek(scheduleLegRequest.dayOfTheWeek());
        Schedule schedule = scheduleLeg.getSchedule();
        schedule.getScheduleLegs().add(scheduleLeg);
        scheduleRepository.save(schedule);
        return ScheduleLegResponse.builder()
                .id(scheduleLeg.getId().toString())
                .scheduleId(scheduleLeg.getSchedule().getId().toString())
                .departureTime(scheduleLeg.getDepartureTime().toString())
                .arrivalTime(scheduleLeg.getArrivalTime().toString())
                .from(scheduleLeg.getFromDestination().toString())
                .to(scheduleLeg.getToDestination().toString())
                .dayOfTheWeek(scheduleLeg.getDayOfWeek().toString())
                .build();
    }


    @Override
    public List<ScheduleObjectResponse> getSchedules() {
        List<Schedule> scheduleList = scheduleRepository.findAll();
        List<ScheduleObjectResponse> scheduleObjectResponseList = new ArrayList<>();
        for (Schedule schedule : scheduleList) {
            ScheduleObjectResponse scheduleObjectResponse = ScheduleObjectResponse.builder().id(schedule.getId().toString())
                    .route(schedule.getRoute().toString())
                    .validFromDate(schedule.getValidFromDate().toString())
                    .validToDate(schedule.getValidToDate().toString())
                    .build();

            scheduleObjectResponseList.add(scheduleObjectResponse);
        }
        return scheduleObjectResponseList;
    }


    @Override
    public List<ScheduleLegResponse> getScheduleLegs() {
        List<ScheduleLegResponse> ScheduleLegResponseList = new ArrayList<>();
        List<Schedule> scheduleList = scheduleRepository.findAll();
        for(Schedule schedule : scheduleList) {
            List<ScheduleLeg> scheduleLegList = schedule.getScheduleLegs().stream().toList();
            for(ScheduleLeg scheduleLeg: scheduleLegList)
            {
                ScheduleLegResponse scheduleLegResponse = ScheduleLegResponse.builder().id(scheduleLeg.getId().toString())
                        .scheduleId(scheduleLeg.getSchedule().getId().toString())
                        .departureTime(scheduleLeg.getDepartureTime().toString())
                        .arrivalTime(scheduleLeg.getArrivalTime().toString())
                        .from(scheduleLeg.getFromDestination().getLabel())
                        .to(scheduleLeg.getToDestination().getLabel())
                        .dayOfTheWeek(scheduleLeg.getDayOfWeek().toString()).build();
                ScheduleLegResponseList.add(scheduleLegResponse);
            }
        }
        return ScheduleLegResponseList;
    }

    @Override
    public ScheduleScheduleLegResponse getScheduleScheduleLeg(ScheduleScheduleLegRequest scheduleScheduleLegRequest) {
        Schedule schedule = scheduleRepository.findByIdOrThrow(scheduleScheduleLegRequest.scheduleRequest().id());
        ScheduleObjectResponse scheduleObjectResponse = ScheduleObjectResponse.builder()
                .id(schedule.getId().toString())
                .route(schedule.getRoute().getLabel())
                .validFromDate(schedule.getValidFromDate().toString())
                .validToDate(schedule.getValidToDate().toString())
                .build();
        List<ScheduleLegResponse> scheduleLegResponseList = new ArrayList<>();
        for(ScheduleLeg scheduleLeg : schedule.getScheduleLegs()) {
            ScheduleLegResponse scheduleLegResponse = ScheduleLegResponse.builder().id(scheduleLeg.getId().toString())
                    .scheduleId(scheduleLeg.getSchedule().getId().toString())
                    .departureTime(scheduleLeg.getDepartureTime().toString())
                    .arrivalTime(scheduleLeg.getArrivalTime().toString())
                    .from(scheduleLeg.getFromDestination().getLabel())
                    .to(scheduleLeg.getToDestination().getLabel())
                    .dayOfTheWeek(scheduleLeg.getDayOfWeek().toString()).build();
            scheduleLegResponseList.add(scheduleLegResponse);
        }
        return ScheduleScheduleLegResponse.builder()
                .scheduleObjectResponse(scheduleObjectResponse)
                .scheduleLegResponses(scheduleLegResponseList)
                .build();

    }

    @Transactional
    @Override
    public ScheduleScheduleLegResponse addScheduleScheduleLeg(ScheduleScheduleLegRequest scheduleScheduleLegRequest) {
       ScheduleObjectResponse scheduleObjectResponse = addSchedule(scheduleScheduleLegRequest.scheduleRequest());
       List<ScheduleLegResponse> scheduleLegResponseList = new ArrayList<>();
       if(scheduleScheduleLegRequest.scheduleLegRequests() != null)
       {
           for(ScheduleLegRequest scheduleLegRequest : scheduleScheduleLegRequest.scheduleLegRequests()) {
               ScheduleLegRequest scheduleLegRequestWithScheduleId = ScheduleLegRequest.builder()
                       .id(null)
                       .scheduleId(UUID.fromString(scheduleObjectResponse.id()))
                       .to(scheduleLegRequest.to())
                       .from(scheduleLegRequest.from())
                       .departureTime(scheduleLegRequest.departureTime())
                       .arrivalTime(scheduleLegRequest.arrivalTime())
                       .dayOfTheWeek(scheduleLegRequest.dayOfTheWeek())
                       .build();
               ScheduleLegResponse scheduleLegResponse = addScheduleLeg(scheduleLegRequestWithScheduleId);
               scheduleLegResponseList.add(scheduleLegResponse);
           }
       }

       return ScheduleScheduleLegResponse.builder()
               .scheduleObjectResponse(scheduleObjectResponse)
               .scheduleLegResponses(scheduleLegResponseList)
               .build();

    }

    @Override
    public ScheduleScheduleLegResponse updateScheduleScheduleLeg(ScheduleScheduleLegRequest scheduleScheduleLegRequest) {
        if(scheduleScheduleLegRequest.scheduleRequest().id() != null)
            updateSchedule(scheduleScheduleLegRequest.scheduleRequest());
        if(scheduleScheduleLegRequest.scheduleLegRequests() != null)
        {
            for(ScheduleLegRequest scheduleLegRequest : scheduleScheduleLegRequest.scheduleLegRequests()) {
                if(scheduleLegRequest.id() != null)
                    updateScheduleLeg(scheduleLegRequest);
                else
                    addScheduleLeg(scheduleLegRequest);
            }
        }
        return getScheduleScheduleLeg(scheduleScheduleLegRequest);
    }



    @Override
    public ScheduleViewWrapper getScheduleView(Pageable pageable, LocationCarrier locationCarrier, TimeRangeCarrier timeRangeCarrier) {

        var from = locationCarrier.from();
        var to = locationCarrier.to();
        var dayOfWeek = timeRangeCarrier.dayOfWeek();
        var timeRange = timeRangeCarrier.timeRange();

        if(dayOfWeek == null)
            dayOfWeek = LocalDate.now()
                    .getDayOfWeek();


        var pagedResult = this.scheduleLegRepository.findSchedulesWithFilters(from, to,
                dayOfWeek, timeRange, pageable);

        return this.shScheduleMapper.toScheduleViews(pagedResult);
    }


    private List<ScheduleLegBusAssignment> getUniqueLegs(List<ScheduleLegBusAssignment> scheduleLegBusAssignments) {
        return scheduleLegBusAssignments.stream()
                .filter(slba -> !slba
                        .getScheduleLeg()
                        .getToDestination().equals(slba.getScheduleLeg()
                                .getFromDestination())
                )
                .toList();
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
                .startTime(startTime)
                .endTime(endTime)
                .currentDate(LocalDate.now())
                .build();
    }
}