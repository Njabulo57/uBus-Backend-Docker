package org.tracker.ubus.ubus.Components.Trips.TripHistory.Mapper;

import java.util.List;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response.AbstractPastTrip;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response.CoOrdinate;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response.PastTripsPageResponse;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response.StudentPastTripResponse;


@Component
@RequiredArgsConstructor
public class TripHistoryMapper {


    /**
     * this method is made polymorphic to allow for different responses based on a user role
     * @param tripsPage trips that is being mapped
     * @param user user that is making the request
     * @return container that contains the past trips according
     * to the user's role
     */
    public PastTripsPageResponse toPastsTrips(Page<Trip> tripsPage, User user) {

        List<Trip> trips = tripsPage.getContent();

        Collection<AbstractPastTrip> pastTrips = switch (user.getRole()) {
            case STAFF, STUDENT -> toStudentPastTripResponses(trips);
            case DRIVER, ADMIN, SUPER_ADMIN -> throw new UnsupportedOperationException("Driver and Admin past trips not supported");

        };


        var totalPages = tripsPage.getTotalPages();
        var pageNumber = tripsPage.getNumber();
        var pageSize = tripsPage.getSize();

        return new PastTripsPageResponse(pastTrips, totalPages, pageNumber, pageSize);
    }



    private List<CoOrdinate> toCoOrdinates(Trip trip) {
        return trip.getTripHistoryPoints().stream()
                .map(historyPoint -> CoOrdinate.builder()
                        .latitude(historyPoint.getLatitude())
                        .longitude(historyPoint.getLongitude())
                        .build()
                )
                .toList();
    };

    private AbstractPastTrip toStudentPastTripResponse(Trip trip) {

        var route = trip.getRoute().getRouteAbbreviated();
        var fromCampus = trip.getRoute().getFromDestination().getLabel();
        var toCampus = trip.getRoute().getToDestination().getLabel();

        Collection<CoOrdinate> coOrdinates = toCoOrdinates(trip);
        return StudentPastTripResponse.builder()
                .route(route)
                .fromCampus(fromCampus)
                .toCampus(toCampus)
                .wasDelayed(false)
                .departureTime(trip.getDepartureTime())
                .arrivalTime(trip.getUpdatedAt())
                .coOrdinates(coOrdinates)
                .build();
    }

    private AbstractPastTrip toDriverPastTripResponse(Trip trip) {
        return null;
    }

    private List<AbstractPastTrip> toDriverPastTripResponses(Collection<Trip> trips) {
        return trips.stream()
                .map(this::toDriverPastTripResponse)
                .toList();
    }

    private List<AbstractPastTrip> toStudentPastTripResponses(Collection<Trip> trips) {
        return trips.stream()
                .map(this::toStudentPastTripResponse)
                .toList();
    }

    private List<AbstractPastTrip> toAdminPastTripsResponses(Collection<Trip> trips) {
        return null;
    }
}