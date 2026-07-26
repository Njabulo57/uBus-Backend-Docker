package org.tracker.ubus.ubus.Components.Buses.BusPreference.Mapper;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPrefView;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Buses.BusUserPreferenceDentination.Entity.BusUserPreferenceDestination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Responses.UserProfileResponse;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class BusPreferenceMapper {

//    public BusPreferenceResponse toResponse(List<BusPreference> busPreferences) {
//        List<String> stringPreferences = new ArrayList<>();
//        if (busPreferences != null)
//            for (BusPreference busPreference : busPreferences) {
//                stringPreferences.add(busPreference.getRoute().getLabel());
//            }
//
//
//        return BusPreferenceResponse.builder()
//                .busPreferences(stringPreferences)
//                .build();
//    }


    public BusPreference toEntity(User user, Route route) {

        return BusPreference.builder()
                .user(user)
                .route(route)
                .build();
    }


    public BusPreferenceResponse toResponse(List<BusUserPreferenceDestination> busUserPreferenceDestinations) {

        var busPrefResponse = new BusPreferenceResponse(new ArrayList<>());

        for(var pref: busUserPreferenceDestinations) {
            busPrefResponse.busPreferences()
                    .add(new BusPrefView(pref.getFromDestination(), pref.getToDestination()));
        }
        return busPrefResponse;
    }


    public BusUserPreferenceDestination toEntity(User user, BusPreference busPreference, Destination to, Destination from) {

        return BusUserPreferenceDestination.builder()
                .fromDestination(from)
                .toDestination(to)
                .busPreference(busPreference)
                .build();
    }
}

