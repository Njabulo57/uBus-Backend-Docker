package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Mapper;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Route.DTOs.Internal.RouteSegmentKey;
import org.tracker.ubus.ubus.Components.Trips.Trip.Util.EtaCalculator;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses.DriverTodayScheduleResponse;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses.ScheduleResponse;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Collection;

@Component
public class ScheduleMapper {

    private final Cache<RouteSegmentKey, List<LatLon>> latLonCache;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");


    public ScheduleMapper(@Qualifier("defaultBusRouteCoordinatesCache") Cache<RouteSegmentKey, List<LatLon>> latLonCache) {
        this.latLonCache = latLonCache;
    }

    public Collection<DriverTodayScheduleResponse> toDriverTodaySchedule(List<Schedule> todaySchedule) {
        return todaySchedule.stream()
                .map(this::toDriverTodaySchedule)
                .toList();
    }

    public ScheduleResponse toDTO(Schedule schedule) {

        var bus = schedule.getBus();
        var frmDest = schedule.getFromDestination();
        var toDest = schedule.getToDestination();
        var scheduleTripStatus = schedule.getScheduleTripStatus();
        var dayType = getDayType(schedule);

        return ScheduleResponse.builder()
                .id(schedule.getId())
                .busName(bus.getName())
                .fromDestination(frmDest.name())
                .toDestination(toDest.name())
                .fromLabel(frmDest.getLabel())
                .toLabel(toDest.getLabel())
                .serviceDate(schedule.getServiceDate())
                .departureTime(schedule.getDepartureTime())
                .arrivalTime(schedule.getArrivalTime())
                .scheduleTripStatus(scheduleTripStatus.getLabel())
                .dayType(dayType)
                .isDone(schedule.isCompleted())
                .build();
    }

    public DriverTodayScheduleResponse toDriverTodaySchedule(Schedule schedule) {

        var arrivalTime = schedule.getArrivalTime();
        var strArrivalTime = formatTime(arrivalTime);

        var departureTime = schedule.getDepartureTime();
        var strDepartureTime = formatTime(departureTime);

        var fromDest = schedule.getFromDestination();
        var toDest = schedule.getToDestination();

        var isCompleted = schedule.isCompleted();

        var routeSegment = RouteSegmentKey.of(schedule.getRoute(), fromDest, toDest);
        var routeSegmentCoordinates = latLonCache.getIfPresent(routeSegment);

        if(routeSegmentCoordinates == null)
            throw new IllegalStateException("Route segment coordinates not found");

        var startLat = routeSegmentCoordinates
                .getFirst();


        var startLng = routeSegmentCoordinates
                .getLast();

        return DriverTodayScheduleResponse.builder()
                .arrivalTime(strArrivalTime)
                .departureTime(strDepartureTime)
                .from(fromDest)
                .start(startLat)
                .end(startLng)
                .isCompleted(isCompleted)
                .to(toDest)
                .build();

    }


    private String formatTime(LocalTime time) {
        return time.format(timeFormatter);
    }



    public String getDayType(Schedule schedule) {
        var serviceDate = schedule.getServiceDate();
        var day = serviceDate.getDayOfWeek();

        if(day.equals(DayOfWeek.SUNDAY))
            return "Sunday";
        if(day.equals(DayOfWeek.SATURDAY))
            return "Saturday";

        return "Weekday";
    }
}

