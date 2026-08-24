package org.tracker.ubus.ubus.Components.Buses.BusPreference.CacheManager;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceClosestTripResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;


import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class UserPreferenceCacheConfig {


    @Bean
    public ConcurrentHashMap<User, BusPreferenceClosestTripResponse> userPreferenceCache() {
        return new ConcurrentHashMap<>();
    }

    @Bean public ConcurrentHashMap<User, BusPreference> busPreferenceCache() {
        return new ConcurrentHashMap<>();
    }


}
