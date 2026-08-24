package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Mapper;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Enum.DriverSchedule;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Route.DTOs.Internal.RouteSegmentKey;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses.*;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLeg;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLegBusAssignment;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class ScheduleMapper {

    private final static Locale ENGLISH_LOCALE = Locale.ENGLISH;
    private final static TextStyle DAY_TEXT_STYLE = TextStyle.SHORT;


    //formats the date
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("dd-MMM-yyyy", ENGLISH_LOCALE);

    private final Cache<RouteSegmentKey, List<LatLon>> latLonCache;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    //formatter for eg. 22:00
    private final DateTimeFormatter timeFormatterWithClassifier = DateTimeFormatter.ofPattern("HH:mm");


    public ScheduleMapper(@Qualifier("defaultBusRouteCoordinatesCache") Cache<RouteSegmentKey, List<LatLon>> latLonCache) {
        this.latLonCache = latLonCache;
    }


    public ScheduleViewWrapper toScheduleViews(Page<ScheduleLeg> scheduleLegs) {

        var totalElements = (int) scheduleLegs.getTotalElements();
        var pageNumber = scheduleLegs.getNumber();
        var pageSize = scheduleLegs.getSize();
        var totalPages = scheduleLegs.getTotalPages();

        var scheduleViewResponses = scheduleLegs.getContent()
                .stream()
                .map( scheduleLeg -> {

                    var totalAvailableBuses = scheduleLeg.getBusesAssigned()
                            .size();
                    var route = scheduleLeg.getSchedule()
                            .getRoute();

                    var formattedDepartureTime = formatTimeWithClassifier(scheduleLeg.getDepartureTime());
                    var formattedArrivalTime = formatTimeWithClassifier(scheduleLeg.getArrivalTime());

                    if(scheduleLeg.getToDestination()
                            .equals(scheduleLeg.getFromDestination())
                    )
                        return null;
                    return ScheduleViewResponse.builder()
                            .routeIdentifier(route.getFormattedName())
                            .from(scheduleLeg.getFromDestination())
                            .to(scheduleLeg.getToDestination())
                            .departureTime(formattedDepartureTime)
                            .arrivalTime(formattedArrivalTime)
                            .busesAvailable(totalAvailableBuses)
                            .route(route)
                            .routeLabel(route.getLabel())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        return ScheduleViewWrapper.builder()
                .scheduleViewResponses(scheduleViewResponses)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .build();
    }

    public Collection<DriverTodayScheduleResponse> toDriverTodaySchedule(List<ScheduleLegBusAssignment> todaySchedule, List<Trip> trips) {
        return todaySchedule.stream()
                .map(scheduleLegBusAssignment ->
                        this.toDriverTodaySchedule(scheduleLegBusAssignment, trips))
                .filter(Objects::nonNull)
                .toList();
    }

    public ScheduleResponse toDTO(ScheduleLegBusAssignment scheduleLegBusAssignment) {

        var scheduleLeg = scheduleLegBusAssignment
                .getScheduleLeg();

        var frmDest = scheduleLeg.getFromDestination();
        var toDest = scheduleLeg.getToDestination();
        var dayType = getDayType();



        return ScheduleResponse.builder()
                .id(scheduleLegBusAssignment.getId())
                .fromDestination(frmDest.name())
                .toDestination(toDest.name())
                .fromLabel(frmDest.getLabel())
                .toLabel(toDest.getLabel())
                .serviceDate(LocalDate.now())
                .departureTime(scheduleLeg.getDepartureTime())
                .arrivalTime(scheduleLeg.getArrivalTime())
                .dayType(dayType)
                .isDone(scheduleLegBusAssignment.isCompleted())
                .build();
    }

    public DriverTodayScheduleResponse toDriverTodaySchedule(ScheduleLegBusAssignment scheduleLegBusAssignment, List<Trip> trips) {

        System.err.println("preparing to map");
        var scheduleLeg = scheduleLegBusAssignment.getScheduleLeg();

        var arrivalTime = scheduleLeg.getArrivalTime();
        var strArrivalTime = formatTime(arrivalTime);

        var departureTime = scheduleLeg.getDepartureTime();
        var strDepartureTime = formatTime(departureTime);

        var fromDest = scheduleLeg.getFromDestination();
        var toDest = scheduleLeg.getToDestination();

        var isCompleted = trips.stream()
                .anyMatch(trip -> trip.getScheduleLegBusAssignment().getId()
                        .equals(scheduleLegBusAssignment.getId())
                        && trip.getStatus() == TripStatus.COMPLETE);


        var route = scheduleLeg.getSchedule()
                .getRoute();

        if(fromDest == toDest) {
            System.err.println("from and to are equal. found: " + fromDest + " "  + toDest);
            return null;
        }
        System.err.println("from and to are not equal");
        var routeSegment = RouteSegmentKey.of(route, fromDest, toDest);
        var routeSegmentCoordinates = latLonCache.getIfPresent(routeSegment);


        if(routeSegmentCoordinates == null)
            throw new IllegalStateException("Route segment coordinates not found FOR " + routeSegment);

        var startLat = routeSegmentCoordinates
                .getFirst();


        var startLng = routeSegmentCoordinates
                .getLast();

        var result = DriverTodayScheduleResponse.builder()
                .arrivalTime(strArrivalTime)
                .departureTime(strDepartureTime)
                .from(fromDest)
                .start(startLat)
                .end(startLng)
                .isCompleted(isCompleted)
                .to(toDest)
                .build();


        System.err.println(result);
        return result;
    }



    public Collection<ScheduleRouteWrapperResponse> getSchedulesByRoute(SequencedCollection<Schedule> schedules) {

        if(schedules.isEmpty() || schedules.size() == 1)
            return Collections.emptyList();


        var driverShifts = DriverSchedule.asCollection(); //getting all possible driver shifts

        return driverShifts.stream()
                .map(shift -> {

                    var shiftStartTime = shift.getStartTime();
                    var formattedStart = this.formatTime(shiftStartTime);

                    var shiftEndTime = shift.getEndTime();
                    var formattedEnd = this.formatTime(shiftEndTime);


                    var scheduleRouteResponses = this.getScheduleRouteResponses(shift, schedules);
                    return ScheduleRouteWrapperResponse.builder()
                            .shift(shift.getLabel())
                            .startTimestamp(formattedStart)
                            .endTimestamp(formattedEnd)
                            .scheduleRows(scheduleRouteResponses)
                            .build();
                } ).toList();
    }

    private Collection<ScheduleRouteResponse> getScheduleRouteResponses(DriverSchedule shift, Collection<Schedule> routeSchedules) {
        return Collections.emptyList();
    }


    private Map<Route, List<Schedule>> groupSchedulesByRoutes(Collection<Schedule> scheduleRows) {
        return scheduleRows.stream()
                .collect(Collectors.groupingBy(Schedule::getRoute,
                        () -> new EnumMap<>(Route.class),
                        Collectors.toList())
                );
    }

    private String formatEffectivePeriod(List<LocalDate> effectivePeriod) {
        if (effectivePeriod.size() > 1)
            return effectivePeriod.getFirst().format(DATE_TIME_FORMATTER) + " to " +
                    effectivePeriod.getLast().format(DATE_TIME_FORMATTER);

        return "NOT ACCOUNTED FOR";
    }

    private String formatDaysOfTheWeek(List<DayOfWeek> listOfDays) {

        listOfDays.sort(Comparator.comparing(DayOfWeek::getValue)); //sort from  first day to last

        String formattedDays;
        if(listOfDays.size() > 1)
            return listOfDays.stream()
                    .map(dayOfWeek -> dayOfWeek.getDisplayName(DAY_TEXT_STYLE, ENGLISH_LOCALE))
                    .collect(Collectors.joining(", "));
        else
            formattedDays = "NOT ACCOUNTED FOR";
        return formattedDays;
    }

    private String mapDestinations(Collection<Destination>  destinations) {
        return destinations.stream()
                .distinct()
                .map(Destination::name)
                .reduce((dest1, dest2) -> dest1+ ",  " + dest2)
                .orElse(null);
    }

    private String formatTime(LocalTime time) {
        return time.format(timeFormatter);
    }

    private String formatTimeWithClassifier(LocalTime time) {
        return time.format(timeFormatterWithClassifier);
    }

    private String getDayType() {
        var serviceDate = LocalDate.now();
        var day = serviceDate.getDayOfWeek();

        if(day.equals(DayOfWeek.SUNDAY))
            return "Sunday";
        if(day.equals(DayOfWeek.SATURDAY))
            return "Saturday";

        return "Weekday";
    }


    private boolean isTimeWithinShift(DriverSchedule driverShift, LocalTime departure) {
        return departure.isAfter(driverShift.getStartTime()) && departure.isBefore(driverShift.getEndTime());
    }


}

