package org.tracker.ubus.ubus.Components.Trips.Trip.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Attendence.AttendanceManager;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.BusTrackingTripManager.BusTrackingCoOrdinatesManager;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.Publisher.MultiEventPublisher;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;
import org.tracker.ubus.ubus.Components.Trips.Trip.CacheManager.TripCacheManager;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripEndRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripRegisterCoordinates;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripStartRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.TripUserOnTappedOutCardEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Events.TripUserOnTappedCardEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Service.Interface.ITripService;
import org.tracker.ubus.ubus.Components.Trips.Trip.TripMapper.TripMapper;
import org.tracker.ubus.ubus.Components.Trips.Trip.Util.TripDriverScheduleManager;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Enum.TripUserStatus;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Mapper.TripUserMapper;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Repository.TripUserRepository;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;



import static org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus.LOADING_PASSENGERS;
import static org.tracker.ubus.ubus.Components.Trips.TripUser.Enum.TripUserStatus.CONTINUED_TO_NEXT;


@Service
@RequiredArgsConstructor
public class TripService extends BaseService implements ITripService {


    private final TripMapper tripMapper;
    private final Set<UUID> busProximityStatus;
    private final MultiEventPublisher multiEventPublisher;

    private final TripUserMapper tripUserMapper;
    private final TripCacheManager tripCacheManager;
    private final AttendanceManager attendanceManager;
    private final TripDriverScheduleManager tripDriverScheduleManager;
    private final BusTrackingCoOrdinatesManager busTrackingCoOrdinatesManager;


    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripUserRepository tripUserRepository;
    private final BusAssignmentRepository busAssignmentRepository;



    @Transactional
    public void startTrip(TripStartRequest tripStartRequest) {

        //getFromTripSimulationCache trip from cache
        var tripId = tripStartRequest.tripId();
        var trip = this.tripCacheManager.getTripFromDemonstrationCacheOrThrow(tripId);
        BusAssignment busAssignment = trip.getBusAssignment();
        Bus bus = busAssignment.getBus();
        bus.setActivityStatus(BusActivityStatus.ON_TRIP); //set the bus activity status to ON_TRIP
        trip.setStatus(TripStatus.IN_PROGRESS);



        this.busRepository.save(bus);
        this.tripRepository.save(trip);
        this.tripCacheManager.putInTripDemonstrationCache(trip); //saving the bus state to cache
    }


    @Override
    @Transactional
    public UUID registerTrip(TripRegisterCoordinates tripRegisterCoordinates) {

        var driver = getCurrentUser();
        var busAssigment = busAssignmentRepository.findByDriverOrThrow(driver);
        var bus = busAssigment.getBus();

        bus.setActivityStatus(LOADING_PASSENGERS); //set the bus activity status to LOADING_PASSENGERS
        this.busRepository.save(bus); //save the new bus state


        var scheduleLegBusAssignment = this.tripDriverScheduleManager.getDriverCurrentScheduleOrThrow(busAssigment);
        var scheduleLeg = scheduleLegBusAssignment.getScheduleLeg();


        var lat = tripRegisterCoordinates.latitude();
        var lng = tripRegisterCoordinates.longitude();

        //validating if the where the driver is located is the same as the trip's starting location
        Destination.validateLocationByCoordinatesOrThrow(lat, lng, scheduleLeg.getFromDestination());

        var route = scheduleLegBusAssignment.getScheduleLeg()
                .getSchedule()
                .getRoute();

        var trip = this.tripMapper.toEntity(busAssigment, route, scheduleLegBusAssignment); //creating the trip
        this.tripRepository.save(trip); //saving the trip
        var savedTrip = this.tripRepository.findByIdOrThrow(trip.getId());

        Trip lastTrip = tripRepository.findLatestTripByBusAssignment(busAssigment);
        //if the last trip is complete, then assign the remaining users to the new trip
        if (lastTrip != null && lastTrip.getStatus() == TripStatus.COMPLETE)
            this.assignLastTripUsersToNextTrip(lastTrip, savedTrip);

        //save trip to cache
        this.tripCacheManager.putInTripDemonstrationCache(savedTrip);
        this.busTrackingCoOrdinatesManager.addFirstLocation(lat, lng, savedTrip.getId());
        return savedTrip.getId();
    }


    @Override
    @Transactional
    public void endTrip(TripEndRequest endTripRequest) {

        var lat = endTripRequest.latitude();
        var lng = endTripRequest.longitude();

        var trip = this.tripRepository.findByIdOrThrow(endTripRequest.tripId());

        if(trip.getStatus() != TripStatus.IN_PROGRESS)
            throw new IllegalStateException("Trip is not in progress");

        var scheduleAssignment = trip.getScheduleLegBusAssignment();
        var scheduleLeg = scheduleAssignment.getScheduleLeg();

        Destination.validateLocationByCoordinatesOrThrow(lat, lng,
                scheduleLeg.getToDestination());


        trip.setStatus(TripStatus.COMPLETE); //mark the trip as complete
        trip.setActualArrivalTime(LocalDateTime.now()); //set the actual arrival time to now
        BusAssignment busAssignment = trip.getBusAssignment();
        Bus bus = busAssignment.getBus();
        bus.setActivityStatus(BusActivityStatus.STATIONERY);
        this.busRepository.save(bus);

        this.markNotTaggedUsersForNextTrip(trip);  //take not tagged users to the next trip
        this.tripRepository.save(trip);


        this.busProximityStatus.remove(trip.getId());

        var driver = getCurrentUser();
        this.attendanceManager.trySignAttendanceForDriver(trip, driver);
        this.busTrackingCoOrdinatesManager.removeTrip(trip.getId()); //removing from the tracking
        this.tripCacheManager.removeFromTripDemonstrationCache(trip); //remove the trip from cache
        this.tripDriverScheduleManager.updateDriverCurrentSchedule(scheduleAssignment); // update the driver schedule



    }


    @Override
    @Transactional
    public int handleNfcTap(UUID tripId, String nfcCode) {
        User user = this.userRepository.findByNfcCodeOrThrow(nfcCode);
        Trip trip = this.tripRepository.findByIdOrThrow(tripId);
        TripUser tripUser = this.tripUserRepository.findByTripAndUser(trip, user);

        trip.incrementPassengerCount();
        tripRepository.save(trip);

        if(tripUser == null) {
            this.createEntrance(user, trip);
            return 0;
        }
        else if(tripUser.getStatus() == TripUserStatus.IN_BUS) {
            this.exitBus(trip, tripUser);
            return 1;
        }
        else {
            this.enterBus(tripUser);
            return 0;
        }

    }



    private void createEntrance(User user, Trip trip) {
        TripUser tripUser = TripUser.builder()
                .user(user)
                .trip(trip)
                .build();
        this.tripUserRepository.save(tripUser);

        var jwtToken = this.getUserJwtToken();
        this.multiEventPublisher.publish(()-> new TripUserOnTappedCardEvent(this, jwtToken, trip));
    }

    private void enterBus(TripUser tripUser) {
        tripUser.setStatus(TripUserStatus.IN_BUS);
        this.tripUserRepository.save(tripUser);
    }

    private void exitBus(Trip trip, TripUser tripUser) {
        tripUser.setStatus(TripUserStatus.EXITED);
        this.tripUserRepository.save(tripUser);

        var jwtToken = this.getUserJwtToken();
        this.multiEventPublisher.publish(()-> new TripUserOnTappedOutCardEvent(this, jwtToken, trip));
    }

    private void assignLastTripUsersToNextTrip(Trip lastTrip, Trip newTrip) {
        List<TripUser> remainingUsers = tripUserRepository.findAllByTripAndStatus(lastTrip, CONTINUED_TO_NEXT);
        var nextTripUsers = this.tripUserMapper.toNextTripEntities(remainingUsers, newTrip);
        this.tripUserRepository.saveAll(nextTripUsers);

    }

    private void markNotTaggedUsersForNextTrip(Trip trip) {
        List<TripUser> remainingUsers = tripUserRepository.findAllByTripAndStatus(trip, TripUserStatus.IN_BUS);
        remainingUsers.forEach(tripUser ->
                tripUser.setStatus(CONTINUED_TO_NEXT)
        );
        tripUserRepository.saveAll(remainingUsers);
    }

}
