package org.tracker.ubus.ubus.Components.Trips.Trip.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Responses.DriverCurrentLocationResponse;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Event.Socket.BusTrackingLocationDeliveryEvent;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;
import org.tracker.ubus.ubus.Components.Trips.Trip.CacheManager.TripCacheManager;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripEndRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripRegisterCoordinates;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Request.TripStartRequest;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.ActiveTripResponse;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.TripUserOnTappedOutCardEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Events.TripBusActivityStatusChangeEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.Events.TripUserOnTappedCardEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Service.Interface.ITripService;
import org.tracker.ubus.ubus.Components.Trips.Trip.TripMapper.TripMapper;
import org.tracker.ubus.ubus.Components.Trips.Trip.Util.EtaCalculator;
import org.tracker.ubus.ubus.Components.Trips.Trip.Util.TripDriverScheduleGetter;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Enum.TripUserStatus;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Mapper.TripUserMapper;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Repository.TripUserRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus.LOADING_PASSENGERS;
import static org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus.ON_TRIP;


@Service
@RequiredArgsConstructor
public class TripService extends BaseService implements ITripService {


    private final TripMapper tripMapper;
    private final TripUserMapper tripUserMapper;
    private final TripCacheManager tripCacheManager;
    private final MultiEvenPublisher multiEvenPublisher;

    private final TripDriverScheduleGetter tripDriverScheduleGetter;

    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripUserRepository tripUserRepository;
    private final ScheduleRepository scheduleRepository;
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

        this.multiEvenPublisher.publish(() ->
                new TripBusActivityStatusChangeEvent(this, tripId, ON_TRIP)
        );

        this.tripCacheManager.putInTripDemonstrationCache(trip); //savig the bus state to cache
    }


    @Override
    @Transactional
    public UUID registerTrip(TripRegisterCoordinates tripRegisterCoordinates) {

        var driver = getCurrentUser();
        var busAssigment = busAssignmentRepository.findByDriverOrThrow(driver);
        var bus = busAssigment.getBus();

        bus.setActivityStatus(LOADING_PASSENGERS); //set the bus activity status to LOADING_PASSENGERS
        this.busRepository.save(bus); //save the new bus state

        Trip lastTrip = tripRepository.findLatestTripByBusAssignment(busAssigment);
        var schedule = this.tripDriverScheduleGetter.getDriverCurrentScheduleOrThrow(busAssigment);

        var lat = tripRegisterCoordinates.latitude();
        var lng = tripRegisterCoordinates.longitude();

        //validating if the where the driver is located is the same as the trip's starting location
        var driverLocation = Destination.findDestinationByCoordinatesOrThrow(lat, lng);

        if (!schedule.getFromDestination().equals(driverLocation))
            throw new IllegalStateException("Driver location is not the same as the trip's starting location");

        var route = schedule.getRoute();
        var trip = this.tripMapper.toEntity(busAssigment, route, schedule); //creating the trip
        this.tripRepository.save(trip); //saving the trip
        var savedTrip = this.tripRepository.findByIdOrThrow(trip.getId());

        //if the last trip is complete, then assign the remaining users to the new trip
        if (lastTrip != null && lastTrip.getStatus() == TripStatus.COMPLETE)
            this.assignLastTripUsersToNextTrip(lastTrip, savedTrip, TripUserStatus.CONTINUED_TO_NEXT);


        //save trip to cache
        this.tripCacheManager.putInTripDemonstrationCache(savedTrip);
        var driverName = formatName(driver);


        var message = DriverCurrentLocationResponse.builder()
                .tripId(savedTrip.getId())
                .latitude(lat)
                .longitude(lng)
                .speed(0)
                .route(trip.getRoute().getLabel())
                .eta("Loading Passengers")
                .busName(bus.getName())
                .busId(bus.getId())
                .driverName(driverName)
                .build();
        this.multiEvenPublisher.publish(()
                -> new BusTrackingLocationDeliveryEvent(this, message)
        );

        return savedTrip.getId();
    }


    @Override
    @Transactional
    public void endTrip(TripEndRequest endTripRequest) {

        var lat = endTripRequest.latitude();
        var lng = endTripRequest.longitude();
        var driverLocation = Destination.findDestinationByCoordinatesOrThrow(lat, lng);

        var trip = this.tripRepository.findByIdOrThrow(endTripRequest.tripId());

        if(trip.getStatus() != TripStatus.IN_PROGRESS)
            throw new IllegalStateException("Trip is not in progress");

        var schedule = trip.getSchedule();
        if(schedule == null)
            throw new IllegalStateException("Trip has no schedule");


        if(!schedule.getToDestination().equals(driverLocation))
            throw new IllegalStateException("Driver location is not the same as the trip's starting location");

        schedule.setCompleted(true); //mark the schedule as completed
        trip.setStatus(TripStatus.COMPLETE); //mark the trip as complete
        BusAssignment busAssignment = trip.getBusAssignment();
        Bus bus = busAssignment.getBus();
        bus.setActivityStatus(BusActivityStatus.STATIONERY);
        this.busRepository.save(bus);

        this.markNotTaggedUsersForNextTrip(trip);  //take not tagged users to the next trip

        this.scheduleRepository.save(schedule); //save the schedule state
        this.tripRepository.save(trip);

        this.tripCacheManager.removeFromTripDemonstrationCache(trip); //remove the trip from cache
    }


    @Transactional
    @Override
    public int handleNfcTap(UUID tripId, String nfcCode) {
        User user = this.userRepository.findByNfcCodeOrThrow(nfcCode);
        Trip trip = this.tripRepository.findByIdOrThrow(tripId);
        TripUser tripUser = this.tripUserRepository.findByTripAndUser(trip, user);

        if(tripUser == null) {
            this.createEntrance(tripId, nfcCode, user, trip);
            return 0;
        }
        else if(tripUser.getStatus() == TripUserStatus.IN_BUS) {
            this.exitBus(tripId, nfcCode, user, trip, tripUser);
            return 1;
        }
        else {
            this.enterBus(tripId, nfcCode, user, trip, tripUser);
            return 0;
        }
    }


    private void createEntrance(UUID tripId, String nfcCode, User user, Trip trip) {
        TripUser tripUser = TripUser.builder()
                .user(user)
                .trip(trip)
                .build();
        this.tripUserRepository.save(tripUser);

         var jwtToken = this.getUserJwtToken();
        this.multiEvenPublisher.publish(()-> new TripUserOnTappedCardEvent(this, jwtToken, trip));
    }

    private void enterBus(UUID tripId, String nfcCode, User user, Trip trip, TripUser tripUser) {
        tripUser.setStatus(TripUserStatus.IN_BUS);
        this.tripUserRepository.save(tripUser);
    }


    private void exitBus(UUID tripId, String nfcCode, User user, Trip trip, TripUser tripUser) {
        tripUser.setStatus(TripUserStatus.EXITED);
        this.tripUserRepository.save(tripUser);

        var jwtToken = this.getUserJwtToken();
        this.multiEvenPublisher.publish(()-> new TripUserOnTappedOutCardEvent(this, jwtToken, trip));
    }


    @Override
    public List<ActiveTripResponse> getActiveTrips() {

        LocalDate today = LocalDate.of(2026, 7, 25);
        LocalTime noon = LocalTime.of(12, 0); // 12:00 AM
        LocalTime morningThreshold = LocalTime.of(9, 30); // 9:30 AM


        var allTrips = this.tripRepository.findByStatus(TripStatus.IN_PROGRESS)
                .stream()
                .filter(trip -> isTripToday(trip, today))
                .filter(trip -> isWithinHours(4, trip.getDepartureTime()))
                .filter(trip -> !trip.getSchedule().isCompleted())
                .map(this::mapToActiveTripResponse)
                .toList();


        return allTrips;
    }


    @Override
    public ActiveTripResponse getActiveTrip(UUID tripId) {
        var trip = this.tripRepository.findByIdOrThrow(tripId);
        return this.tripMapper.toDTO(trip);
    }


    private void assignLastTripUsersToNextTrip(Trip lastTrip, Trip newTrip,TripUserStatus status) {
        List<TripUser> remainingUsers = tripUserRepository.findAllByTripAndStatus(lastTrip, status);
        var nextTripUsers = this.tripUserMapper.toNextTripEntities(remainingUsers, newTrip);
        this.tripUserRepository.saveAll(nextTripUsers);

    }

    private void markNotTaggedUsersForNextTrip(Trip trip) {
        List<TripUser> remainingUsers = tripUserRepository.findAllByTripAndStatus(trip, TripUserStatus.IN_BUS);
        remainingUsers.forEach(tripUser ->
                tripUser.setStatus(TripUserStatus.CONTINUED_TO_NEXT)
        );
        tripUserRepository.saveAll(remainingUsers);
    }


    private boolean isWithinNextHour(Trip trip) {
        LocalDateTime departureTime = trip.getDepartureTime();
        if (departureTime == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourFromNow = now.plusHours(1);
        return !departureTime.isBefore(now) && departureTime.isBefore(oneHourFromNow);
    }


    private boolean isTripToday(Trip trip, LocalDate today) {
        LocalDateTime departureTime = trip.getDepartureTime();
        if (departureTime == null) {
            return false;
        }
        LocalDate tripDate = departureTime.toLocalDate();
        return tripDate.equals(today);
    }

    private boolean isTripMonday(Trip trip, LocalDate monday) {
        LocalDateTime departureTime = trip.getDepartureTime();
        if (departureTime == null) {
            return false;
        }
        LocalDate tripDate = departureTime.toLocalDate();
        return tripDate.equals(monday);
    }

    private boolean isTripYesterday(Trip trip, LocalDate today) {
        var departureTime = trip.getDepartureTime();
        LocalDate tripDate = departureTime.toLocalDate();
        return tripDate.equals(today.minusDays(1));
    }

    private ActiveTripResponse mapToActiveTripResponse(Trip trip) {
        return this.tripMapper.toDTO(trip);
    }

    private boolean isWithinHours(int hours, LocalDateTime departureTime) {
        return departureTime.isAfter(LocalDateTime.now().minusHours(hours));
    }

    private String formatName(User user) {

        var firstName = user.getFirstname();
        var firstNameInitialCapitalized = Character.toUpperCase(firstName.charAt(0));
        return firstNameInitialCapitalized + ". " + user.getLastname();
    }
}
