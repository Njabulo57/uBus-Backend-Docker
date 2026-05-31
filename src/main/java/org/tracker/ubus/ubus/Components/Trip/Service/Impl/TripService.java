package org.tracker.ubus.ubus.Components.Trip.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.BusRoute.Entity.BusRoute;
import org.tracker.ubus.ubus.Components.BusRoute.Repository.BusRouteRepository;
import org.tracker.ubus.ubus.Components.Trip.DTO.Request.TripRegisterCoordinates;
import org.tracker.ubus.ubus.Components.Trip.DTO.Response.ActiveTripResponse;
import org.tracker.ubus.ubus.Components.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trip.Service.Interface.ITripService;
import org.tracker.ubus.ubus.Components.Trip.TripMapper.TripMapper;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;

import java.util.*;


@Service
@RequiredArgsConstructor
public class TripService implements ITripService {

    private final TripMapper tripMapper;
    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final BusRouteRepository busRouteRepository;
    private final BusAssignmentRepository busAssignmentRepository;


    @Override
    @Transactional
    public void registerTrip(TripRegisterCoordinates tripRegisterCoordinates) {

        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        var driverLoggedIn = (UserPrincipal) authentication.getPrincipal();
        var driverEntity = driverLoggedIn.getUser();


        var busAssignment = this.busAssignmentRepository.findByDriverOrThrow(driverEntity);
        var busRoute = this.busRouteRepository.findByBusOrThrow(busAssignment.getBus());


        var validStatingRoute = this.validateTripStartLocation(tripRegisterCoordinates, busRoute);


        var trip = this.tripMapper.toEntity(busRoute, busAssignment, validStatingRoute);

        var bus = busAssignment.getBus();
        bus.setActivityStatus(BusActivityStatus.LOADING_PASSENGERS);

        this.busRepository.save(bus); 
        this.tripRepository.save(trip);
    }



    @Override
    public ActiveTripResponse getBusAssignedTrip() {

        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        var driverLoggedIn = (UserPrincipal) authentication.getPrincipal();

        var driverEntity = driverLoggedIn.getUser();

        var busAssignment = this.busAssignmentRepository.findByDriverOrThrow(driverEntity);
        var trip = this.tripRepository.findByBusAssignmentOrThrow(busAssignment);

        return this.tripMapper.toDTO(trip);
    }


    @Override
    public List<ActiveTripResponse> getActiveTrips() {
        var activeTrips = this.tripRepository.findByStatus(TripStatus.IN_PROGRESS);
        return this.tripMapper.toDTO(activeTrips);
    }

    @Override
    public ActiveTripResponse getActiveTrip(UUID tripId) {
        var trip = this.tripRepository.findByIdOrThrow(tripId);
        return this.tripMapper.toDTO(trip);
    }


    /**
     * returns back the route that is was valid for the trip to begin
     * @param tripRegisterCoordinates the coordinates of the where the trip is starting
     * @param busRoute bus route of the bus that is being assigned to the trip
     * @return returns the route that is valid for the trip to begin
     */
    private Route validateTripStartLocation(TripRegisterCoordinates tripRegisterCoordinates, BusRoute busRoute) {

        Map<Boolean, Route> routeMap = new HashMap<>();


        var latitude = tripRegisterCoordinates.latitude();
        var longitude = tripRegisterCoordinates.longitude();

        var route = busRoute.getRoute(); //get the default trip
        var reverseTrip = route.getReverseTrip(); //get the reverse trip

        boolean toRouteCondition = route.isValidLocation(latitude, longitude);
        boolean fromRouteCondition = reverseTrip.isValidLocation(latitude, longitude);

        if(toRouteCondition && fromRouteCondition)
            throw new IllegalStateException("Invalid start and End Location for trip");
        //both can never be true at the same time because of geo-fencing


        routeMap.put(toRouteCondition, route);
        routeMap.put(fromRouteCondition, reverseTrip);

        return routeMap.get(true);
    }



}
