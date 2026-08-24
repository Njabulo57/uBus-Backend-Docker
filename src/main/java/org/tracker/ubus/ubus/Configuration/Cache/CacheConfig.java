package org.tracker.ubus.ubus.Configuration.Cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.LatLon;
import org.tracker.ubus.ubus.Components.Route.DTOs.Internal.RouteSegmentKey;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses.ScheduleBaseInformation;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@EnableCaching
@Configuration
public class CacheConfig {


    @Bean
    public CacheManager generalPurposeCache() {
        var cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .maximumSize(5)
                .recordStats()
        );
        return cacheManager;
    }



    /**
     * Creates a cache for storing the mapping of {@link RouteSegmentKey} objects to lists of {@link LatLon} coordinates.
     * The cache is configured with a maximum size of 100 entries and entries expire 24 hours after being written.
     * This cache is used to store precomputed or frequently accessed route coordinates to improve efficiency.
     *
     * @return a {@link Cache} instance that maps {@link RouteSegmentKey} to {@link List} of {@link LatLon}.
     */
    @Bean(name = "defaultBusRouteCoordinatesCache")
    public Cache<RouteSegmentKey, List<LatLon>> defaultBusRouteCoordinatesCache() {

        return Caffeine.newBuilder()
                .maximumSize(100) // the default segments are around 14-16
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build();
    }

    /**
     * this caches the route taken by a driver if it deviates from the default route
     * @return returns a cache of route segments
     */
    @Bean(name = "changedBusRouteCoordinatesCache")
    public Cache<RouteSegmentKey, List<LatLon>> busRouteChangeCache() {
        return Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    @Bean
    public Cache<UUID, DelayStatus> latestBusEtaCache() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    @Bean
    public Cache<UUID, Trip> tripCache() {
        return Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(70, TimeUnit.HOURS)
                .build();
    }

}


