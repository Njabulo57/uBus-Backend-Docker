package org.tracker.ubus.ubus.Components.Trips.TripReport.Mapper;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripReport.Entity.TripReport;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.function.Predicate;
//this is far from correct btw
@Component
public class TripReportMapper {



    public TripReport toEntity(Trip trip, User user) {


        var toCampus = trip.getRoute().getToDestination();
        var fromCampus = trip.getRoute().getFromDestination();

        Predicate<TripUser> isStudent = tripUser -> tripUser.getUser()
                .getRole().equals(UserRole.STUDENT);

        int studentCount = getUserCount(trip.getTripUsers(), isStudent);
        int staffCount = trip.getTotalCount() - studentCount;


        return TripReport.builder()
                .trip(trip)
                .generatedBy(user)
                .route(trip.getRoute())
                .fromDestination(toCampus)
                .toDestination(fromCampus)
                .departureTime(trip.getDepartureTime())
                .arrivalTime(trip.getUpdatedAt())

                .totalPassengers(trip.getTotalCount())
                .totalStaff(staffCount)
                .totalStudents(studentCount)
                .wasDelayed(false)
                .delayMinutes(LocalDateTime.now().getMinute())
                .delayReason("report reason")
                .build();
    }


    private int getUserCount(Collection<TripUser> tripUsers, Predicate<TripUser> filterer) {
        return (int) tripUsers.stream()
                .filter(filterer)
                .count();
    }
}
