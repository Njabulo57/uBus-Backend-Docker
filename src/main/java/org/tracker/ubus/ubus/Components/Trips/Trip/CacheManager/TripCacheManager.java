package org.tracker.ubus.ubus.Components.Trips.Trip.CacheManager;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TripCacheManager {

    private final Cache<UUID, Trip> tripCache;
    private final Map<UUID, Map<Trip, SequencedCollection<Destination>>> tripCollectionMapCache;


    public Collection<Trip> getAll() {
        var tripCacheList = this.tripCache.asMap()
                .values()
                .stream()
                .toList();
        var demonstationTrips = this.tripCollectionMapCache.values()
                .stream()
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        demonstationTrips.addAll(tripCacheList);
        return demonstationTrips;
    }


    public void putInTripSimulationCache(UUID tripId, Trip trip) {
        this.tripCache.put(tripId, trip);
    }

    public void putInTripDemonstrationCache(Trip trip) {
        var tripMap = this.of(trip);
        this.tripCollectionMapCache.put(trip.getId(), tripMap);
    }

    public void putAllInTripSimulationCache(Collection<Trip> trips) {
        trips.forEach(trip ->
                this.tripCache.put(trip.getId(), trip));
    }

    public void putAllInTripSimulationCache(Map<UUID, Trip> trips) {
        this.tripCache.putAll(trips);
    }

    public void putAllInTripDemonstrationCache(Collection<Trip> trips) {
        trips.forEach(this::putInTripDemonstrationCache);
    }



    public Trip getFromTripSimulationCache(UUID tripId) {
        return this.tripCache.getIfPresent(tripId);
    }

    public Trip getFromTripSimulationCacheOrThrow(UUID tripId) {
        Trip trip = this.tripCache.getIfPresent(tripId);
        if (trip == null)
            throw new IllegalArgumentException("Trip not found in Simulation Cache");
        return trip;
    }

    public Collection<Trip> getAllFromTripSimulationCacheAsCollection() {
        return this.tripCache.asMap().values();
    }

    public List<Trip> getAllFromTripSimulationCacheAsList() {
        return this.tripCache.asMap().values().stream().toList();
    }


    public Map<UUID, Trip> getAllFromTripSimulationCacheAsMap() {
        return this.tripCache.asMap();
    }



    public Map<Trip, SequencedCollection<Destination>> getFromTripDemonstrationCache(UUID tripId) {
        return this.tripCollectionMapCache.get(tripId);
    }

    public Map<Trip, SequencedCollection<Destination>> getFromTripDemonstrationCacheOrThrow(UUID tripId) {
        Map<Trip, SequencedCollection<Destination>> tripMap = this.tripCollectionMapCache.get(tripId);
        if (tripMap == null)
            throw new IllegalArgumentException("Trip not found in Demonstration Cache");
        return tripMap;
    }

    public Trip getTripFromDemonstrationCache(UUID tripId) {
        var tripMap = this.tripCollectionMapCache.get(tripId);
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
        var tripMap = this.tripCollectionMapCache.get(tripId);
        if (tripMap == null)
            return null;
        return tripMap.values().stream().findFirst().orElse(null);
    }

    public Set<Trip> getAllFromTripDemonstrationCacheAsSet() {
        return this.tripCollectionMapCache
                .keySet()
                .stream()
                .map(this::getTripFromDemonstrationCache)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Collection<Trip> getAllFromTripDemonstrationCacheAsCollection() {
        return this.tripCollectionMapCache.values()
                .stream()
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .toList();
    }

    public Collection<Map<Trip, SequencedCollection<Destination>>> getAllFromTripDemonstrationCache() {
        return this.tripCollectionMapCache.values();
    }

    public Map<UUID, Map<Trip, SequencedCollection<Destination>>> getAllFromTripDemonstrationCacheAsMap() {
        return this.tripCollectionMapCache;
    }


    public void removeFromTripSimulationCache(UUID tripId) {
        this.tripCache.invalidate(tripId);
    }

    public void removeFromTripDemonstrationCache(UUID tripId) {
        this.tripCollectionMapCache.remove(tripId);
    }

    public void removeFromTripDemonstrationCache(Trip trip) {
        this.tripCollectionMapCache.remove(trip.getId());
    }

    public void clearTripSimulationCache() {
        this.tripCache.invalidateAll();
    }

    public void clearTripDemonstrationCache() {
        this.tripCollectionMapCache.clear();
    }


    public long estimatedTripSimulationCacheSize() {
        return this.tripCache.estimatedSize();
    }

    public int tripDemonstrationCacheSize() {
        return this.tripCollectionMapCache.size();
    }

    public boolean containsInTripSimulationCache(UUID tripId) {
        return this.tripCache.asMap().containsKey(tripId);
    }

    public boolean containsInTripDemonstrationCache(UUID tripId) {
        return this.tripCollectionMapCache.containsKey(tripId);
    }

    public boolean containsTripInDemonstrationCache(Trip trip) {
        return this.tripCollectionMapCache.containsKey(trip.getId());
    }


    private Map<Trip, SequencedCollection<Destination>> of(Trip trip) {
        var map = new HashMap<Trip, SequencedCollection<Destination>>();

        var schedule = trip.getSchedule();
        var from = schedule.getFromDestination();
        var to = schedule.getToDestination();

        var destinations = List.of(from, to);
        map.put(trip, destinations);
        return map;
    }
}