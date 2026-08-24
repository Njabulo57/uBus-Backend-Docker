package org.tracker.ubus.ubus.Components.SIMULATION;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.LocalTime;
import java.util.*;


@Configuration
public class SimulatorStopsCacheConfig {


    @Bean
    public Map<Trip, LocalTime> predictions() {
        return new HashMap<>();
    }


    @Bean
    public Map<UUID,Map<Trip, SequencedCollection<Destination>>> tripCollectionMap() {
        return new HashMap<>();
    }


    @Bean
    public Map<Route, Map<SequencedCollection<Destination>, Integer>> routeDestinationMap() {
        return new HashMap<>();
    }
}
