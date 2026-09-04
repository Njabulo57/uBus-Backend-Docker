package org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Impl;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Request.BusPreferenceDTO;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPrefView;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceClosestTripResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Events.BusPreferenceSubscriberAllTripsEvent;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Events.BusPreferenceSubscriberEvent;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Exceptions.BusPreferenceMaximumException;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Mapper.BusPreferenceMapper;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Repository.BusPreferenceRepository;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Interface.IBusPreferenceService;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.Publisher.MultiEventPublisher;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;
import org.tracker.ubus.ubus.Configuration.WebSocket.Monitor.WebSocketSubscriptionMonitor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;



@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BusPreferenceService extends BaseService implements IBusPreferenceService {

    @Autowired
    private BusPreferenceRepository repository;
    private final UserRepository userRepository;

    private final BusPreferenceMapper busPreferenceMapper;
    private final MultiEventPublisher multiEventPublisher;

    private static final int TOTAL_PREFERENCE_BUSES_TO_SHOW = 2;

    private static final String PREFERENCE_SOCKET_CONNECTION = "/topic/bus-preference-suscriber/";
    private static final String ALL_TRIPS_SOCKET_CONNECTION = "/topic/bus-preference-suscriber-all-trips/";


    private final BusPreferenceTripLoader busPreferenceTripLoader;

    private final ConcurrentHashMap<User, BusPreferenceClosestTripResponse> busPreferenceCache;

    private final WebSocketSubscriptionMonitor webSocketSubscriptionMonitor;

    private final List<User> users = new ArrayList<>();

    @PostConstruct
    protected void init() {

        var activeStatus = UserStatus.ACTIVE;
        var roles = List.of(UserRole.STAFF, UserRole.STUDENT);
        var allStudentsAndStaff =userRepository.findByRoleInAndStatus(roles,  activeStatus);

        this.users.addAll(allStudentsAndStaff);
    }


    @Override
    public void addPreference(List<BusPreferenceDTO> busPreferenceDTOs) {

        Map<Destination, Destination> routeMap = new HashMap<>();
        User user = getCurrentUser();
        List<BusPreference> busPreferences = repository.findAllByUser(user);
        if(busPreferences.size() >= 3)
            throw new BusPreferenceMaximumException("You can only have up to 3 preferences");

        for(BusPreferenceDTO busPreferenceDTO : busPreferenceDTOs) {
            boolean toBeAdded = true;
            Destination from = Destination.valueOf(busPreferenceDTO.getFrom());
            Destination to = Destination.valueOf(busPreferenceDTO.getTo());
            for(BusPreference busPreference : busPreferences) {
                if (busPreference.getFromDestination() == from && busPreference.getToDestination() == to) {
                    toBeAdded = false;
                    break;
                }

            }
            for(Map.Entry<Destination, Destination> entry: routeMap.entrySet()) {
                if(entry.getKey() == from && entry.getValue() == to) {
                    toBeAdded = false;
                    break;
                }
            }
            if(toBeAdded)
                routeMap.put(from, to);

        }
        for(Map.Entry<Destination, Destination> entry: routeMap.entrySet()) {
            BusPreference busPreference = BusPreference.builder().
                    user(user).
                    fromDestination(entry.getKey()).
                    toDestination(entry.getValue()).
                    build();
            repository.save(busPreference);
        }


    }


    @Transactional
    @Override
    public void editPreference(BusPreferenceDTO busPreferenceDTO) {
        deletePreference(busPreferenceDTO);
        addPreference(List.of(busPreferenceDTO));
    }


    @Override
    public void deletePreference(BusPreferenceDTO busPreferenceDTO) {
        User user = getCurrentUser();
        List<BusPreference> busPreferences = repository.findAllByUser(user);
        for(BusPreference busPreference : busPreferences) {

            if(busPreference.getFromDestination() == Destination.valueOf(busPreferenceDTO.getFrom()) &&
                    busPreference.getToDestination() == Destination.valueOf(busPreferenceDTO.getTo())) {
                repository.delete(busPreference);
            }
        }
    }


    @Override
    public BusPreferenceResponse viewPreferences() {

        var user = getCurrentUser();

        var preferences = this.repository.findAllByUser(user);

        var allPreferences = preferences.stream()
                .map(pref -> new BusPrefView(pref.getFromDestination(),
                        pref.getToDestination())
                )
                .toList();
        ;


        return BusPreferenceResponse
                .of(allPreferences);
    }


    @Override
    public Collection<BusPrefView> getAllBusPreferences() {
        return Route.getAllValidDestinationCombinations();
    }


    @Override
    public boolean hasBusPreference() {
        var user = getCurrentUser();
        return this.repository.existsByUser(user);
    }


    @Scheduled(fixedDelay = 3_000)
    protected void checkBusDistanceToUsers() {

        this.showAllTripsByPreference(); //shows trips coming to the user's destinations

        //if no one is connected to the endpoint dealing with preferences
        if(this.users.isEmpty())
            return;
        this.showAllTrips();
    }


    private void showAllTripsByPreference() {
        var usersWithPreferences = this.repository.findUsersWithPreferences();

        var nearestBusesByUserPreference = this.busPreferenceTripLoader
                .getNearestBusesToPreference(TOTAL_PREFERENCE_BUSES_TO_SHOW,
                        usersWithPreferences);

        for(var userPrefNearestBus: nearestBusesByUserPreference) {

            var user = userPrefNearestBus.user();
            this.multiEventPublisher.publish(() ->
                    new BusPreferenceSubscriberEvent(this, user, userPrefNearestBus ));
        }

    }


    private void showAllTrips() {

        var allNearestTripsMap = this.busPreferenceTripLoader.getNearestTripsToTheirDestination();

        for (var entry : allNearestTripsMap.entrySet()) {
            var nearestTripToDest = entry.getValue();

            for (var destTrip : nearestTripToDest.entrySet()) {
                var closetTripInfo = destTrip.getValue();
                var trip = closetTripInfo.getTrip();
                var eta = closetTripInfo.getEta();
                var distance = closetTripInfo.getDistance();
                var progress = closetTripInfo.getProgress();

                var delayStatus = closetTripInfo.getDelayStatus();
                this.multiEventPublisher.publish(()->
                        new BusPreferenceSubscriberAllTripsEvent(this, users, trip,
                                eta, distance, delayStatus, progress));
            }
        }

    }
}
