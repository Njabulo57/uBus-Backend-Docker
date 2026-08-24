package org.tracker.ubus.ubus.Components.Trips.Trip.CacheManager;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripCacheManager {

    private final Cache<UUID, Trip> tripsFromSimulation;
    private final Map<UUID, Map<Trip, SequencedCollection<Destination>>> tripsFromDemonstration;


    public Collection<Trip> getAll() {
        var tripCacheList = this.tripsFromSimulation.asMap()
                .values()
                .stream()
                .filter(Trip::isFromSimulation)
                .toList();

        var demonstrationTrips = this.tripsFromDemonstration.values()
                .stream()
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .filter(trip -> !trip.isFromSimulation())
                .toList();

        var totalTrips = new ArrayList<>(demonstrationTrips);
        totalTrips.addAll(tripCacheList);
        return totalTrips;
    }




    public void putInTripSimulationCache(UUID tripId, Trip trip) {
        this.tripsFromSimulation.put(tripId, trip);
    }

    public void putInTripDemonstrationCache(Trip trip) {
        var tripMap = this.of(trip);
        this.tripsFromDemonstration.put(trip.getId(), tripMap);
    }

    public void putAllInTripSimulationCache(Collection<Trip> trips) {
        trips.forEach(trip ->
                this.tripsFromSimulation.put(trip.getId(), trip));
    }

    public void putAllInTripSimulationCache(Map<UUID, Trip> trips) {
        this.tripsFromSimulation.putAll(trips);
    }

    public void putAllInTripDemonstrationCache(Collection<Trip> trips) {
        trips.forEach(this::putInTripDemonstrationCache);
    }



    public Trip getFromTripSimulationCache(UUID tripId) {
        return this.tripsFromSimulation.getIfPresent(tripId);
    }

    public Trip getFromTripSimulationCacheOrThrow(UUID tripId) {
        Trip trip = this.tripsFromSimulation.getIfPresent(tripId);
        if (trip == null)
            throw new IllegalArgumentException("Trip not found in Simulation Cache");
        return trip;
    }

    public Collection<Trip> getAllFromTripSimulationCacheAsCollection() {
        return this.tripsFromSimulation.asMap().values();
    }

    public List<Trip> getAllFromTripSimulationCacheAsList() {
        return this.tripsFromSimulation.asMap().values().stream().toList();
    }


    public Map<UUID, Trip> getAllFromTripSimulationCacheAsMap() {
        return this.tripsFromSimulation.asMap();
    }



    public Map<Trip, SequencedCollection<Destination>> getFromTripDemonstrationCache(UUID tripId) {
        return this.tripsFromDemonstration.get(tripId);
    }

    public Map<Trip, SequencedCollection<Destination>> getFromTripDemonstrationCacheOrThrow(UUID tripId) {
        Map<Trip, SequencedCollection<Destination>> tripMap = this.tripsFromDemonstration.get(tripId);
        if (tripMap == null)
            throw new IllegalArgumentException("Trip not found in Demonstration Cache");
        return tripMap;
    }


    public Trip getTripFromDemonstrationCache(UUID tripId) {
        var tripMap = this.tripsFromDemonstration.get(tripId);
        if (tripMap == null)
            return null;
        return tripMap.keySet()
                .stream()
                .findFirst()
                .orElse(null);
    }

    public Trip getTripFromDemonstrationCacheOrThrow(UUID tripId) {
        Trip trip = getTripFromDemonstrationCache(tripId);
        if (trip == null)
            throw new IllegalArgumentException("Trip not found in Demonstration Cache");
        return trip;
    }

    public SequencedCollection<Destination> getDestinationsFromDemonstrationCache(UUID tripId) {
        var tripMap = this.tripsFromDemonstration.get(tripId);
        if (tripMap == null)
            return null;
        return tripMap.values().stream().findFirst().orElse(null);
    }

    public Set<Trip> getAllFromTripDemonstrationCacheAsSet() {
        return this.tripsFromDemonstration
                .keySet()
                .stream()
                .map(this::getTripFromDemonstrationCache)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Collection<Trip> getAllFromTripDemonstrationCacheAsCollection() {
        return this.tripsFromDemonstration.values()
                .stream()
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .toList();
    }

    public Collection<Map<Trip, SequencedCollection<Destination>>> getAllFromTripDemonstrationCache() {
        return this.tripsFromDemonstration.values();
    }

    public Map<UUID, Map<Trip, SequencedCollection<Destination>>> getAllFromTripDemonstrationCacheAsMap() {
        return this.tripsFromDemonstration;
    }


    public void removeFromTripSimulationCache(UUID tripId) {
        this.tripsFromSimulation.invalidate(tripId);
    }

    public void removeFromTripDemonstrationCache(UUID tripId) {
        this.tripsFromDemonstration.remove(tripId);
    }

    public void removeFromTripDemonstrationCache(Trip trip) {
        this.tripsFromDemonstration.remove(trip.getId());
    }

    public void removeFromAll(Trip trip) {
        if(this.containsTripInDemonstrationCache(trip))
            this.removeFromTripDemonstrationCache(trip);

        if(this.containsInTripSimulationCache(trip.getId()))
            this.removeFromTripSimulationCache(trip.getId());
    }

    public void clearTripSimulationCache() {
        this.tripsFromSimulation.invalidateAll();
    }

    public void clearTripDemonstrationCache() {
        this.tripsFromDemonstration.clear();
    }


    public long estimatedTripSimulationCacheSize() {
        return this.tripsFromSimulation.estimatedSize();
    }

    public long estimatedTotal() {
        return this.estimatedTripSimulationCacheSize() + this.tripDemonstrationCacheSize();
    }

    public int tripDemonstrationCacheSize() {
        return this.tripsFromDemonstration.size();
    }

    public boolean containsInTripSimulationCache(UUID tripId) {
        return this.tripsFromSimulation.asMap().containsKey(tripId);
    }

    public boolean containsInTripDemonstrationCache(UUID tripId) {
        return this.tripsFromDemonstration.containsKey(tripId);
    }

    public boolean containsTripInDemonstrationCache(Trip trip) {
        return this.tripsFromDemonstration.containsKey(trip.getId());
    }


    private Map<Trip, SequencedCollection<Destination>> of(Trip trip) {
        var map = new HashMap<Trip, SequencedCollection<Destination>>();

        var schedule = trip.getScheduleLegBusAssignment()
                .getScheduleLeg();

        var from = schedule.getFromDestination();
        var to = schedule.getToDestination();

        var destinations = List.of(from, to);
        map.put(trip, destinations);
        return map;
    }
}