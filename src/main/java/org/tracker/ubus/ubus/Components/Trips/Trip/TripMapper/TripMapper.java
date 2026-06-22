package org.tracker.ubus.ubus.Components.Trips.Trip.TripMapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.*;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response.AbstractPastTrip;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response.AdminPastTripBusView;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response.AdminPastTripDriverView;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response.AdminPastTripViewResponse;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.Entity.TripHistoryPoint;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;

import java.util.*;
import java.util.function.Predicate;

@Component
public class TripMapper {


    public Trip toEntity(BusAssignment busAssignment, Route route) {
        return Trip.builder()
                .route(route)
                .status(TripStatus.IN_PROGRESS)
                .busAssignment(busAssignment)
                .totalCount(0)
                .build();
    }

    public List<ActiveTripResponse> toDTO(List<Trip> trips) {
        return trips.stream()
                .map(this::toDTO)
                .toList();
    }

    public ActiveTripResponse toDTO(Trip trip) {
        var busAssignment = trip.getBusAssignment();
        var bus = busAssignment.getBus();
        var busId = bus.getId();
        var route = trip.getRoute();

        var busStatus = bus.getActivityStatus().getLabel();
        return ActiveTripResponse.builder()
                .id(trip.getId())
                .route(route.getLabel())
                .busName(bus.getName())
                .busStatus(busStatus)
                .busId(busId)
                .build();
    }


    public Page<AbstractPastTrip> toDTOs(User user, Page<Trip> tripsPage,
                                         Map<Trip, List<TripHistoryPoint>> tripHistoryPointsMap) {
        return switch (user.getRole()) {
            case STAFF, STUDENT -> Page.empty();
            case DRIVER -> Page.empty();
            case ADMIN -> tripsPage.map(trip -> toDTO(trip, tripHistoryPointsMap.get(trip)));
            default -> Page.empty();
        };
    }


    public List<AbstractPastTrip> toDTOs(User user, Collection<Trip> trips,
                                         Map<Trip, Set<TripHistoryPoint>> tripHistoryPointsMap) {
        return switch (user.getRole()) {
            case ADMIN -> toDTOs(trips, tripHistoryPointsMap);
            default -> Collections.emptyList();
        };
    }

    public List<AbstractPastTrip> toDTOs(Collection<Trip> trips,
                                         Map<Trip, Set<TripHistoryPoint>> tripHistoryPoints) {
        return trips.stream()
                .map(trip -> (AbstractPastTrip) toDTO(trip, tripHistoryPoints.get(trip)))
                .toList();
    }

    private AdminPastTripViewResponse toDTO(Trip trip, Collection<TripHistoryPoint> tripHistoryPoints) {
        var busAssignment = trip.getBusAssignment();
        var bus = busAssignment.getBus();

        var busView = toDTO(bus);
        var driverView = toDTO(busAssignment.getDriver());
        var route = trip.getRoute();

        Predicate<TripUser> isStudent = tripUser ->
                tripUser.getUser().getRole() == UserRole.STUDENT;

        var tripUsers = trip.getTripUsers();
        int studentCount = this.getTotalPassengerByType(tripUsers, isStudent);
        int staffCount = this.getTotalPassengerByType(tripUsers, isStudent.negate());

        var fromCampus = route.getFromDestination().getLabel();
        var toCampus = route.getToDestination().getLabel();

        return AdminPastTripViewResponse.builder()
                .totalPassengers(trip.getTotalCount())
                .totalSeats(bus.getCapacity())
                .totalStaff(staffCount)
                .totalStudents(studentCount)
                .bus(busView)
                .driver(driverView)
                .route(route.name())
                .fromCampus(fromCampus)
                .toCampus(toCampus)
                .wasDelayed(false)
                .departureTime(trip.getDepartureTime())
                .arrivalTime(trip.getUpdatedAt())
                .build();
    }

    private AdminPastTripBusView toDTO(Bus bus) {
        return AdminPastTripBusView.builder()
                .busName(bus.getName())
                .busType(bus.getType().getLabel())
                .build();
    }

    private AdminPastTripDriverView toDTO(User user) {
        return AdminPastTripDriverView.builder()
                .driverPhone(user.getPhoneNumber())
                .driverName(user.getFirstname())
                .build();
    }

    private int getTotalPassengerByType(Collection<TripUser> usersOnStrip,
                                        Predicate<TripUser> passengerFilterer) {
        return (int) usersOnStrip.stream()
                .filter(passengerFilterer)
                .count();
    }
}