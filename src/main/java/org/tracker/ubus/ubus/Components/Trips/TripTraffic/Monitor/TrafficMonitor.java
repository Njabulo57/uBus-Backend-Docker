package org.tracker.ubus.ubus.Components.Trips.TripTraffic.Monitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.Handlers.DefaultRouteServiceCacheHandler;
import org.tracker.ubus.ubus.Components.Trips.Trip.CacheManager.TripCacheManager;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.Mapper.TrafficMapper;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.Repository.TrafficMonitorRepository;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.Service.TrafficService;
import org.tracker.ubus.ubus.Configuration.ExternalClients.TomTom.Service.TomTomService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficMonitor {


    private final TomTomService tomTomService;

    private final TrafficService trafficService;
    private final TrafficMonitorRepository trafficMonitorRepository;
    private final TripCacheManager tripCacheManager;
    private final DefaultRouteServiceCacheHandler defaultRouteServiceCacheHandler;
    private final Collection<UUID> busesToMonitorForTraffic = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<UUID, ConcurrentLinkedDeque<DriverCurrentLocationMessage>> busStates;
    private final ConcurrentMap<UUID, Map<LocalTime, LocalTime>> busInitialAndLastTraffic = new ConcurrentHashMap<>();

    private static final double TRAFFIC_SPEED = 15;
    private static final int SPEEDS_FOR_AVERAGE_THRESHOLD = 7;
    private static final int TRAFFIC_DELAY_THRESHOLD_MINUTES = 5;


    @Scheduled(fixedDelay = 10_000)
    protected void addBusToMonitor() {
        log.info("Checking for buses in traffic...");
        var buses = this.findBusesToMonitor();
        if (!buses.isEmpty()) {
            log.info("Found " + buses.size() + " buses in traffic: " + buses);
            this.busesToMonitorForTraffic.addAll(buses);
        } else
            log.info("No buses currently in traffic");

        System.err.println("Total buses being monitored: " + this.busesToMonitorForTraffic.size());
    }

    @Scheduled(fixedDelay = 5_000)
    protected void monitorTraffic() {
        trackBusTimeInTraffic();
        removeFromMonitor();
    }

    private void removeFromMonitor() {
        var toRemove = new ArrayList<UUID>();

        busesToMonitorForTraffic.forEach(tripId -> {
            var messages = busStates.get(tripId);
            if (messages != null && !messages.isEmpty()) {
                var avgSpeed = getSpeedAverage(messages);
                if (avgSpeed >= TRAFFIC_SPEED + 10) {
                    log.info("Bus " + tripId + " cleared traffic. Speed: " + String.format("%.1f", avgSpeed) + " km/h");
                    toRemove.add(tripId);
                }
            }

        });

        for (var tripId : toRemove) {
            var record = busInitialAndLastTraffic.get(tripId);
            if (record != null && !record.isEmpty()) {
                var entryTime = record.keySet().iterator().next();
                var lastTime = record.get(entryTime);
                var duration = (int) Math.abs(delayInMinutes(entryTime, lastTime));
                log.info("Bus " + tripId + " was in traffic for " + duration + " minutes");


                var trafficEntity = this.trafficMonitorRepository.findByTripOrThrow(tripId);
                trafficEntity.setDelayMinutes(duration);
                this.trafficMonitorRepository.save(trafficEntity);

                busesToMonitorForTraffic.remove(tripId);
                busInitialAndLastTraffic.remove(tripId);
            }
        }
    }

    private Collection<UUID> findBusesToMonitor() {

        return busStates.entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .filter(entry -> !entry.getValue().getFirst().isSimulated())
                .filter(entry -> {
                    var avgSpeed = this.getSpeedAverage(entry.getValue());
                    //return buses with speed <= 15 km/h and ont the move
                    return avgSpeed <= TRAFFIC_SPEED && avgSpeed >= 0 && isOnTheMove(this.getTrip(entry.getKey()));
                })
                .map(ConcurrentMap.Entry::getKey)
                .toList();
    }


    private void trackBusTimeInTraffic() {
        this.busesToMonitorForTraffic.forEach(tripId -> {
            var messages = busStates.get(tripId);
            if (messages != null && !messages.isEmpty()) {
                var mapTimeMapper = this.busInitialAndLastTraffic.computeIfAbsent(tripId, id ->
                        new ConcurrentHashMap<>());

                var trip = this.getTrip(tripId);
                if(trip == null) return;



                var currentTime = LocalTime.now();

                if (mapTimeMapper.isEmpty()) {
                    log.info("Bus " + tripId + " entered traffic at " + currentTime);
                    mapTimeMapper.put(currentTime, currentTime);
                } else {
                    var entryTime = mapTimeMapper.keySet().iterator()
                            .next();

                    var duration = Math.abs(delayInMinutes(entryTime, currentTime));
                    if (duration > 0 && duration % 2 == 0)
                        log.debug("Bus " + tripId + " has been in traffic for " + duration + " minutes");

                    mapTimeMapper.clear();
                    mapTimeMapper.put(entryTime, currentTime);
                }
            }
        });

        trackBusTrafficAnalytics();
    }

    private void trackBusTrafficAnalytics() {
        var inTrafficLong = this.busInitialAndLastTraffic.entrySet()
                .stream()
                .filter(entry -> {
                    var timeMap = entry.getValue();
                    if (timeMap.isEmpty()) return false;

                    var entryTime = timeMap.keySet().iterator().next();
                    var lastTime = timeMap.get(entryTime);

                    long delayMinutes = Math.abs(delayInMinutes(entryTime, lastTime));
                    return delayMinutes <= TRAFFIC_DELAY_THRESHOLD_MINUTES;
                })
                .toList();

        if (inTrafficLong.isEmpty()) {
            log.debug("No buses in traffic for " + TRAFFIC_DELAY_THRESHOLD_MINUTES + "+ minutes");
            return;
        }

        inTrafficLong.forEach(entry -> {
            var tripId = entry.getKey();
            var timeMap = entry.getValue();
            var entryTime = timeMap.keySet().iterator().next();
            var lastTime = timeMap.get(entryTime);


            Trip trip = null;
            try {
                var originTimeInTraffic = entry.getValue().keySet().iterator().next();
                var date = LocalDate.now();
                var withTime = LocalDateTime.of(date, originTimeInTraffic);

                var destinations = this.getDestinations(tripId);
                if (destinations == null || destinations.isEmpty()) {
                    log.error("No destinations found for trip " + tripId);
                    return;
                }

                var from = destinations.getFirst();
                var to = destinations.getLast();

                trip = this.getTrip(tripId);
                if (trip == null)
                    return;


                var currentLocation = this.getLastMessage(tripId);
                if (currentLocation == null)
                    return;


                var avgSpeed = this.getSpeedAverage(busStates.get(tripId));
                var remainingDistance = this.getRemainingDistance(trip, currentLocation);

                var tomResponse = this.tomTomService.getRouteWithTraffic(from, to, true);

                this.trafficService.saveOrUpdateTraffic(trip, withTime, currentLocation,
                        avgSpeed, remainingDistance, tomResponse);


            } catch (Exception e) {
                var busName = getBusName(trip);
                System.err.println("Error processing traffic for bus " + busName +  ": " + e.getMessage());
                System.err.println("error: " + e);
            }
        });
    }

    private boolean isOnTheMove(Trip trip) {
        var busAssignment = trip.getBusAssignment();

        return !busAssignment
                .getBus()
                .getActivityStatus()
                .equals(BusActivityStatus.LOADING_PASSENGERS);
    }


    /**
     * Calculates the remaining distance (in kilometers) to the destination for a given trip
     * based on the driver's current location.
     *
     * @param trip the trip for which the remaining distance is being calculated. Provides
     *             information such as the route and the current schedule leg.
     * @param currentLocation the driver's current geolocation data, including latitude
     *                        and longitude, used to determine their current position.
     * @return the remaining distance to the destination in kilometers.
     */
    private double getRemainingDistance(Trip trip, DriverCurrentLocationMessage currentLocation) {
        var route = trip.getRoute();
        var from = trip.getScheduleLegBusAssignment().getScheduleLeg().getFromDestination();
        var to = trip.getScheduleLegBusAssignment().getScheduleLeg().getToDestination();

        var currentLat = currentLocation.latitude();
        var currentLng = currentLocation.longitude();
        var currentPos = LatLon.of(currentLng, currentLat);

        return this.defaultRouteServiceCacheHandler
                .getRemainingDistanceToDestination(route, from, to, currentPos) / 1000.0;
    }

    private double getSpeedAverage(ConcurrentLinkedDeque<DriverCurrentLocationMessage> messages) {
        if (messages == null || messages.isEmpty())
            return 0.0;


        var arrayList = new ArrayList<>(messages);
        var speedCount = Math.min(arrayList.size(), SPEEDS_FOR_AVERAGE_THRESHOLD);
        if (speedCount == 0) return 0.0;

        var startIndex = arrayList.size() - speedCount;
        var speedAccumilation = 0.0;
        for (int i = startIndex; i < arrayList.size(); i++)
            speedAccumilation += arrayList.get(i).speed();

        return speedAccumilation / speedCount;
    }


    private long delayInMinutes(LocalTime time1, LocalTime time2) {
        return (time1.toSecondOfDay() - time2.toSecondOfDay()) / 60;
    }

    private String getBusName(Trip trip) {
        if (trip == null) return "NO NAME";
        return trip.getBusAssignment().getBus().getName();
    }

    private SequencedCollection<Destination> getDestinations(UUID tripId) {
        var dests = tripCacheManager.getDestinationsFromDemonstrationCache(tripId);
        return dests;
    }

    private Trip getTrip(UUID tripId) {
        var trip = tripCacheManager.getTripFromDemonstrationCache(tripId);
        if (trip == null) {
            log.warn("No trip in cache for ID " + tripId);
        }
        return trip;
    }

    private DriverCurrentLocationMessage getLastMessage(UUID tripId) {
        var deque = busStates.get(tripId);
        if (deque == null || deque.isEmpty()) {
            log.warn("No location messages for trip " + tripId);
            return null;
        }
        return deque.getLast();
    }

}