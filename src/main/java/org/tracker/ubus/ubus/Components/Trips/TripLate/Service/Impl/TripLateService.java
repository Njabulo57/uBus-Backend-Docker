package org.tracker.ubus.ubus.Components.Trips.TripLate.Service.Impl;

import com.github.benmanes.caffeine.cache.Cache;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.BusPromixityChecker.Event.BusJourneyProgressionEvent;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.Publisher.MultiEventPublisher;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Service.Impl.TripService;
import org.tracker.ubus.ubus.Components.Trips.Trip.Util.EtaCalculator;
import org.tracker.ubus.ubus.Components.Trips.TripLate.DTO.Request.TripLateRequest;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Entity.TripLate;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Enum.TripLateReason;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Event.BusLateNotificationEvent;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Mapper.TripLateMapper;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Repository.TripLateRepository;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Service.Interface.ITripLateService;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TripLateService extends BaseService implements ITripLateService {


    private final TripLateMapper tripLateMapper;


    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripLateRepository tripLateRepository;

    private final MultiEventPublisher multiEventPublisher;
    private final Cache<UUID, DelayStatus> latestBusEtaCache;
    private List<User> studentsAndStaff = new LinkedList<>();


    @Override
    @Transactional
    public void updateTripLate(TripLateRequest tripLateRequest) {
        var tripId = tripLateRequest.tripId();
        Trip trip = tripRepository.findByIdOrThrow(tripId);
        User user = getCurrentUser();
        if(!trip.getBusAssignment().getDriver()
                .getId().equals(user.getId()))
            throw new IllegalArgumentException("User is not the driver of the trip");

        var tripLateOptional = tripLateRepository.findByTripId(trip.getId());

        TripLate tripLate;
        if (tripLateOptional.isEmpty())
            tripLate = createTripLate(tripLateRequest);
        else {
            var lateTrip = tripLateOptional.get();
            tripLate = this.updateTripLate( lateTrip, tripLateRequest);
        }


        var delayStatus = this.latestBusEtaCache.get(tripId, id -> null);
        if(delayStatus == null)
            return;

        var roles = List.of(UserRole.STUDENT, UserRole.STAFF);
        var status = UserStatus.ACTIVE;

        this.studentsAndStaff = userRepository.findByRoleInAndStatus(roles, status);
        this.multiEventPublisher.publish(() -> new BusLateNotificationEvent(this, tripLate,
                delayStatus, studentsAndStaff)
        );
    }



    private TripLate updateTripLate(TripLate existingTripLate, TripLateRequest tripLateRequest) {
        if (tripLateRequest.description() != null && !tripLateRequest.description().isBlank()) {
            existingTripLate.setReason(TripLateReason.valueOf(tripLateRequest.reason()));
            existingTripLate.setDescription(tripLateRequest.description());
        } else {
            existingTripLate.setReason(TripLateReason.valueOf(tripLateRequest.reason()));
        }
        return tripLateRepository.save(existingTripLate);
    }

    private TripLate createTripLate(TripLateRequest tripLateRequest) {
        UUID tripId = tripLateRequest.tripId();
        Trip trip = tripRepository.findByIdOrThrow(tripId);
        var tripLate = this.tripLateMapper.toEntity(tripLateRequest, trip);
        return tripLateRepository.save(tripLate);
    }

}
