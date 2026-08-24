package org.tracker.ubus.ubus.Components.Buses.BusPreference.Mapper;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPrefView;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.util.ArrayList;
import java.util.List;


@Component
public class BusPreferenceMapper {



    public BusPreference toEntity(User user, Route route) {

        return BusPreference.builder()
                .user(user)
                .fromDestination(null)
                .toDestination(null)
                .build();

    }


}

