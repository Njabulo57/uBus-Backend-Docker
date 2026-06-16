package org.tracker.ubus.ubus.Components.Trips.Trip.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Buses.BusRoute.Entity.BusRoute;
import org.tracker.ubus.ubus.Components.Buses.BusRoute.Repository.BusRouteRepository;
import org.tracker.ubus.ubus.Components.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.Notification.Events.BusDepartureNotification;
import org.tracker.ubus.ubus.Components.Notification.Events.Notification;
import org.tracker.ubus.ubus.Components.Notification.Events.Notifications;
import org.tracker.ubus.ubus.Components.Notification.Service.Impl.NotificationDispatcher;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripEndRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripRegisterCoordinates;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.ActiveTripResponse;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Exceptions.DriverOutSideCampusBoundsException;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Service.Interface.ITripService;
import org.tracker.ubus.ubus.Components.Trips.Trip.TripMapper.TripMapper;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Repository.TripUserRepository;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Campus;
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
    private final TripUserRepository tripUserRepository;
    private final MultiEvenPublisher multiEvenPublisher;
    private final NotificationDispatcher notificationDispatcher;
    private final BusAssignmentRepository busAssignmentRepository;



    @Override
    @Transactional
    public void startTrip(UUID tripId) {

        var trip = this.tripRepository.findByIdOrThrow(tripId);
        trip.setStatus(TripStatus.IN_PROGRESS);
        this.tripRepository.save(trip);

        var usersOnBoard = this.tripUserRepository.findUserIdsByTrip(trip); // get all the users on board
        Queue<UUID> onBoardQueue = new LinkedList<>(usersOnBoard); // adding all the users on board to the queue

        var notification = Notifications.busDepartureNotification(trip.getRoute(), onBoardQueue);
        // send notifications to all users on board that the trip is starting
        notificationDispatcher.sendNotification(notification);
    }


    @Override
    @Transactional
    public void registerTrip(TripRegisterCoordinates tripRegisterCoordinates) {

        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        var driverLoggedIn = (UserPrincipal) authentication.getPrincipal();
        var driverEntity = driverLoggedIn.getUser();


        BusAssignment busAssignment = this.busAssignmentRepository.findByDriverOrThrow(driverEntity);
        BusRoute busRoute = this.busRouteRepository.findByBusOrThrow(busAssignment.getBus());


        Route validStatingRoute = this.validateTripStartLocation(tripRegisterCoordinates, busRoute);


        var trip = this.tripMapper.toEntity(busAssignment, validStatingRoute);

        var bus = busAssignment.getBus();
        bus.setActivityStatus(BusActivityStatus.LOADING_PASSENGERS);

        this.busRepository.save(bus); 
        this.tripRepository.save(trip);
    }


    @Override
    @Transactional
    public void endTrip(TripEndRequest endTripRequest) {

        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        var driverLoggedIn = (UserPrincipal) authentication.getPrincipal();
        var driverEntity = driverLoggedIn.getUser();

        var latitude = endTripRequest.latitude();
        var longitude = endTripRequest.longitude();

        
        var tripId = endTripRequest.tripId();
        var tripStaus = TripStatus.IN_PROGRESS;
        var trip = this.tripRepository.findActiveTripByIdOrThrow(tripStaus,tripId); //get the available trip

        boolean isDriverWithinCampusBounds = this.isDriverWithinCampusBounds(trip.getRoute(), latitude, longitude);

        if(!isDriverWithinCampusBounds) // if the driver is not within the campus bounds
            throw new DriverOutSideCampusBoundsException("Not Within Campus Grounds");


        var totalOnBoard = this.tripUserRepository.countByTrip(trip); //get the total number of students or staff on board

        trip.setStatus(TripStatus.COMPLETE); // mark as complete
        trip.setTotalCount(totalOnBoard); //set the total staff or students on board;
        this.tripRepository.save(trip); //save the trip as completed


        //generate the report
        //this.multiEvenPublisher.publish(() -> new GenerateReportEvent(this, driverEntity, trip));
    }


    @Override
    @Transactional
    public void enterBus(UUID tripId) {

        //hadde i had to write some code to program the notification system
        var trip = this.tripRepository.findByIdOrThrow(tripId);
        var bus = trip.getBusAssignment().getBus();

        int busCapacity = bus.getCapacity();
        int totalPassengers = trip.getTripUsers().size();

        //this should never run
        if(totalPassengers > busCapacity)
            throw new IllegalStateException("trip is full");


        if(totalPassengers < busCapacity) {
            //logic
        }

        if(totalPassengers == busCapacity) {

            //find all the users on board and send a notification to them
            var users = this.tripUserRepository.findUserIdsByTrip(trip);
            Queue<UUID> onBoardQueue = new LinkedList<>(users); // adding all the users on board to the queue
            var busFullNotif = Notifications.busFullNotification(onBoardQueue);
            this.notificationDispatcher.sendNotification(busFullNotif); // send the notification to all users on board

        }
    }


    @Override
    public void exitBus(UUID tripId) {

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
     * @throws IllegalStateException if the trip start and end location
     * are valid at the same time which should never be the case
     */
    private Route validateTripStartLocation(TripRegisterCoordinates tripRegisterCoordinates,
                                            BusRoute busRoute) throws IllegalStateException {

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


    /**
     * Checks if the driver's current location is within the bounds of the campus in the provided route.
     * @param route The route object containing campus information for boundary checking.
     * @param latitude The latitude of the driver's current location.
     * @param longitude The longitude of the driver's current location.
     * @return {@code true} if the driver's location is within the bounds of a campus in the route, {@code false} otherwise.
     */
    private boolean isDriverWithinCampusBounds(Route route, double latitude, double longitude) {

        Optional<Campus> campusMatched = route.getCurrentCampus(latitude, longitude);

        if(campusMatched.isEmpty()) {
            System.err.println("no matching campus found");
            return false;
        }

        var campus = campusMatched.get();
        System.err.println("matching campus found. " + campus.name());
        return true;
    }

}
