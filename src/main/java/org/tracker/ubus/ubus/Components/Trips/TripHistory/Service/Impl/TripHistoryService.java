package org.tracker.ubus.ubus.Components.Trips.TripHistory.Service.Impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.TripMapper.TripMapper;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response.PastTripsPageResponse;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.Entity.TripHistoryPoint;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.Mapper.TripHistoryMapper;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.Service.Interface.ITripHistoryService;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.Utility.LocationDisperser;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Repository.TripUserRepository;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;

import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class TripHistoryService implements ITripHistoryService {


    private final TripMapper tripMapper;
    private final TripRepository tripRepository;
    private final TripHistoryMapper tripHistoryMapper;
    private final TripUserRepository tripUserRepository;
    private final BusAssignmentRepository busAssignmentRepository;

    @Override
    public PastTripsPageResponse getPastTrips(Pageable pageable) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userLoggedIn = (UserPrincipal) authentication.getPrincipal();
        var userEntity = userLoggedIn.getUser();

        Page<Trip> tripsPage = switch (userEntity.getRole()) {
            case STAFF, STUDENT -> {
                Page<TripUser> tripUsers = tripUserRepository.findCompletedTripsByUser(userEntity, pageable);

                yield tripUsers
                        .map(TripUser::getTrip);
            }
            case DRIVER -> {
                var busAssignment = busAssignmentRepository.findByDriverOrThrow(userEntity);
                yield this.tripRepository.findCompletedTripsByDriver(busAssignment, pageable);
            }
            default -> Page.empty();
        };

        this.disperseLocations(tripsPage.getContent());
        return tripHistoryMapper.toPastsTrips(tripsPage, userEntity);
    }


    /**
     * applies douglas peucker algorithm to each trip's history points
     * reduces the number of coordinates while preserving route shape
     *
     * @param trips collection of trips with their history points
     */
    private void disperseLocations(List<Trip> trips) {

        long startTime = System.nanoTime();

        // Process each trip using streams
        Collection<Trip> dispersedTrips = trips.stream()
                .peek(trip -> {

                    List<TripHistoryPoint> points = new ArrayList<>(trip.getTripHistoryPoints());

                    // sorting points by timePosted to ensure correct order
                    points.sort(Comparator.comparing(TripHistoryPoint::getTimePosted));

                    // apply Douglas peucker algorithm
                    Collection<TripHistoryPoint> simplified = LocationDisperser.disperse(points);

                    // Clear existing points and add simplified ones
                    trip.getTripHistoryPoints().clear();
                    trip.getTripHistoryPoints().addAll(simplified);

                })
                .toList();

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.0;

        log.info("Douglas Peucker processing time: {} seconds for {} trips", duration, trips.size());

    }
}