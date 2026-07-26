package org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Impl;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Request.BusPreferenceDTO;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPrefView;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceClosestTripResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Events.BusPreferenceSuscriberAllTripsEvent;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Events.BusPreferenceSuscriberEvent;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Exceptions.BusPreferenceNotFoundException;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Mapper.BusPreferenceMapper;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Repository.BusPreferenceRepository;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Interface.IBusPreferenceService;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Handlers.DefaultRouteServiceCacheHandler;
import org.tracker.ubus.ubus.Components.Buses.BusUserPreferenceDentination.Repository.BusUserPrefDestinationRepository;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus.ACTIVE;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BusPreferenceService implements IBusPreferenceService {

    @Autowired
    private BusPreferenceRepository repository;

    private final UserRepository userRepository;
    private final BusPreferenceMapper busPreferenceMapper;
    private final MultiEvenPublisher multiEvenPublisher;


    private final DefaultRouteServiceCacheHandler defaultRouteServiceCacheHandler;
    private final BusPreferenceTripLoader busPreferenceTripLoader;

    private final BusUserPrefDestinationRepository busUserPrefDestinationRepository;

    private final ConcurrentHashMap<User, BusPreferenceClosestTripResponse> busPreferenceCache;
    private final ConcurrentHashMap<User, BusPreference> preferencesCache;
    private final ConcurrentHashMap<UUID, ConcurrentLinkedDeque<DriverCurrentLocationMessage>> busQueues;


    private final Set<User> allUsers;



    @PostConstruct
    protected void init() {

        var allPreferences = this.repository.findAllPreferences();
        allPreferences.forEach(preference -> {

            this.preferencesCache.put(preference.getUser(),
                    preference);
        });
    }


    @Override
    public void addPreference(List<BusPreferenceDTO> busPreferenceDTOs) {

        if (busPreferenceDTOs.isEmpty() || busPreferenceDTOs.size() > 3)
            return;

        var user = getCurrentUser();
        var routesToSave = new HashSet<Route>();

        // First, collect all unique routes from all combinations
        busPreferenceDTOs.forEach(pref -> {
            var from = Destination.valueOf(pref.getFrom());
            var to = Destination.valueOf(pref.getTo());

            if (from.equals(to))
                throw new BusPreferenceNotFoundException("From and To cannot be the same");

            var destinationArray = new Destination[]{from, to};
            var routes = Route.findRouteByDestinations(destinationArray);
            routesToSave.addAll(routes);
        });

        // Then save each route once
        for (var route : routesToSave) {
            if (repository.existsByUserAndRoute(user, route))
                continue;

            var busPreference = this.busPreferenceMapper.toEntity(user, route);
            var savedPreference = this.repository.save(busPreference);
            this.repository.flush();

            // Save all destination combinations for this route
            busPreferenceDTOs.forEach(pref -> {
                var from = Destination.valueOf(pref.getFrom());
                var to = Destination.valueOf(pref.getTo());
                var destArray = new Destination[]{from, to};
                var routesForPref = Route.findRouteByDestinations(destArray);

                if (routesForPref.contains(route)) {
                    var busUserPrefDestination = this.busPreferenceMapper.toEntity(user, savedPreference, to, from);
                    this.busUserPrefDestinationRepository.save(busUserPrefDestination);
                }
            });
            this.preferencesCache.put(user, savedPreference);
        }
    }


    @Override
    public void editPreference(BusPreferenceDTO busPreferenceDTO) {
        var user = getCurrentUser();
        var oldFrom = Destination.valueOf(busPreferenceDTO.getOldFrom());
        var oldTo = Destination.valueOf(busPreferenceDTO.getOldTo());
        var newFrom = Destination.valueOf(busPreferenceDTO.getFrom());
        var newTo = Destination.valueOf(busPreferenceDTO.getTo());

        if (oldFrom.equals(newFrom) && oldTo.equals(newTo))
            throw new BusPreferenceNotFoundException("No changes detected");

        // Delete old preference
        var oldRoutes = Route.findRouteByDestinations(oldFrom, oldTo);
        for (var route : oldRoutes) {
            var busPreference = this.repository.findByUserAndRoute(user, route);
            if (busPreference != null) {
                var toRemove = busPreference.getBusUserPrefDestinations().stream()
                        .filter(dest -> dest.getFromDestination() == oldFrom && dest.getToDestination() == oldTo)
                        .findFirst()
                        .orElse(null);

                if (toRemove != null) {
                    busPreference.removeBusUserPrefDestination(toRemove);
                    this.busUserPrefDestinationRepository.delete(toRemove);
                    this.busUserPrefDestinationRepository.flush();

                    if (busPreference.getBusUserPrefDestinations().isEmpty()) {
                        this.repository.delete(busPreference);
                        this.repository.flush();
                        this.preferencesCache.remove(user);
                    } else {
                        this.repository.save(busPreference);
                    }
                    this.busPreferenceCache.remove(user);
                }
            }
        }

        // Add new preference
        var newRoutes = Route.findRouteByDestinations(newFrom, newTo);
        for (var route : newRoutes) {
            if (!repository.existsByUserAndRoute(user, route)) {
                var busPreference = this.busPreferenceMapper.toEntity(user, route);
                var savedPreference = this.repository.save(busPreference);
                this.repository.flush();

                var busUserPrefDestination = this.busPreferenceMapper.toEntity(user, savedPreference, newTo, newFrom);
                this.busUserPrefDestinationRepository.save(busUserPrefDestination);

                this.preferencesCache.put(user, savedPreference);
            }
        }
    }


    @Override
    public void deletePreference(BusPreferenceDTO busPreferenceDTO) {
        var user = getCurrentUser();
        var from = Destination.valueOf(busPreferenceDTO.getFrom());
        var to = Destination.valueOf(busPreferenceDTO.getTo());

        var routes = Route.findRouteByDestinations(from, to);

        for (var route : routes) {
            var busPreference = this.repository.findByUserAndRoute(user, route);
            if (busPreference != null) {
                // Find the specific destination to remove
                var toRemove = busPreference.getBusUserPrefDestinations().stream()
                        .filter(dest -> dest.getFromDestination() == from && dest.getToDestination() == to)
                        .findFirst()
                        .orElse(null);

                if (toRemove != null) {
                    // Remove from the collection
                    busPreference.removeBusUserPrefDestination(toRemove);
                    // Delete the destination from database
                    this.busUserPrefDestinationRepository.delete(toRemove);
                    this.busUserPrefDestinationRepository.flush();

                    // If no destinations left, delete the preference
                    if (busPreference.getBusUserPrefDestinations().isEmpty()) {
                        this.repository.delete(busPreference);
                        this.repository.flush();
                        this.preferencesCache.remove(user);
                    }
                    this.busPreferenceCache.remove(user);
                }
            }
        }
    }

    @Override
    public BusPreferenceResponse viewPreferences() {

        var user = getCurrentUser();

        var preferences = this.repository.findAllByUser(user);

        var allPreferences = preferences.stream()
                .flatMap(preference -> preference.getBusUserPrefDestinations().stream())
                .toList();
        return busPreferenceMapper.toResponse(allPreferences);

    }


    @Override
    public BusPrefView[] getAllBusPreferences() {
        return Route.getAllValidDestinationCombinations();
    }


    @Override
    public boolean hasBusPreference() {
        var user = getCurrentUser();
        return this.repository.existsByUser(user);
    }


    @Scheduled(fixedDelay = 5_000)
    protected void checkBusDistanceToUsers() {

        if (!this.preferencesCache.isEmpty())
            this.showAllTripsByPreference(); //shows trips coming to the user's destinations
        this.showAllTrips();
    }

    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                .getPrincipal();
        return Objects.requireNonNull(principal).getUser();
    }


    private void showAllTripsByPreference() {
        var usersWithPreferences = this.repository.findUsersWithPreferences();
        var nearestBusesByUserPreference = this.busPreferenceTripLoader
                .findNearestBusForUserPreference(usersWithPreferences);

        for (var entry : nearestBusesByUserPreference.entrySet()) {
            var user = entry.getKey();
            var userNearestBuses = entry.getValue();

            for(var nearestBusByPref: userNearestBuses ) {
                this.multiEvenPublisher.publish(() ->
                        new BusPreferenceSuscriberEvent(this, user, nearestBusByPref));
            }


        }
    }

    private void showAllTrips() {

        var roles = List.of(UserRole.STUDENT, UserRole.STAFF);
        var allUsers = this.userRepository.findByRoleInAndStatus(roles, ACTIVE); //getFromTripSimulationCache all users from the database
        var allNearestTripsMap = this.busPreferenceTripLoader.getNearestTripsToTheirDestination();


        for (var entry : allNearestTripsMap.entrySet()) {
            var nearestTripToDest = entry.getValue();

            for (var destTrip : nearestTripToDest.entrySet()) {
                var closetTripInfo = destTrip.getValue();
                var trip = closetTripInfo.trip();
                var eta = closetTripInfo.eta();
                var distance = closetTripInfo.distance();

                var delayStatus = closetTripInfo.delayStatus();
                this.multiEvenPublisher.publish(()->
                        new BusPreferenceSuscriberAllTripsEvent(this, allUsers, trip,
                                eta, distance, delayStatus));
            }
        }

    }



}
