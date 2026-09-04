package org.tracker.ubus.ubus.Components.Trips.TripUser.Service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.TripUser.DTOs.Response.OnTripResponse;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripUserService extends BaseService {

    private final TripRepository tripRepository;

    public OnTripResponse isUserCurrentlyOnTrip() {

        var currentUser = getCurrentUser();

        var statuses = List.of(TripStatus.IN_PROGRESS, TripStatus.CREATED);
        var currentTrips = this.tripRepository.findByStatusIn(statuses);

        var tripIdentified = currentTrips.stream()
                .filter(trip -> trip.getTripUsers().stream()
                        .anyMatch(tripUser -> tripUser.getUser()
                                .equals(currentUser)
                        )
                ).findFirst()
                .orElse(null);

        if(tripIdentified == null)
            return OnTripResponse.builder()
                    .isOnTrip(false)
                    .build();

        return OnTripResponse.builder()
                .isOnTrip(true)
                .tripID(tripIdentified.getId())
                .build();
    }
}
