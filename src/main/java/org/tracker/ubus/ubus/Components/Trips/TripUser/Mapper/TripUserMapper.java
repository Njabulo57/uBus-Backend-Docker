package org.tracker.ubus.ubus.Components.Trips.TripUser.Mapper;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Enum.TripUserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.Collection;


@Component
public class TripUserMapper {


    public Collection<TripUser> toNextTripEntities(Collection<TripUser> remainingUsers, Trip newTrip) {

        return remainingUsers.stream()
                .map(tripUser -> {

                    User currentUser = tripUser.getUser();
                    return TripUser.builder()
                            .user(currentUser)
                            .trip(newTrip)
                            .status(TripUserStatus.IN_BUS)
                            .isFirstTrip(false)
                            .build();
                })
                .toList();
    }
}
