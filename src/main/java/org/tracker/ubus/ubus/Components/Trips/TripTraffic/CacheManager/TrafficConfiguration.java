package org.tracker.ubus.ubus.Components.Trips.TripTraffic.CacheManager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.DTO.Internal.TrafficInfoCarrier;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Configuration
public class TrafficConfiguration {


    @Bean
    public Cache<UUID, TrafficInfoCarrier> getTrafficCache() {
        return Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(40, TimeUnit.MINUTES)
                .build();
    }






}

