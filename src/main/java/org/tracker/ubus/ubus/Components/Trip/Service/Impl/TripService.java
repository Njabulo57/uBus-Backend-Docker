package org.tracker.ubus.ubus.Components.Trip.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Trip.DTO.Request.TripRegisterRequest;
import org.tracker.ubus.ubus.Components.Trip.DTO.Response.ActiveTripResponse;
import org.tracker.ubus.ubus.Components.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trip.Service.Interface.ITripService;
import org.tracker.ubus.ubus.Components.Trip.TripMapper.TripMapper;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class TripService implements ITripService {

    private final TripMapper tripMapper;
    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final BusAssignmentRepository busAssignmentRepository;


    @Override
    @Transactional
    public void registerTrip(TripRegisterRequest registerRequest) {

        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        var driverLoggedIn = (UserPrincipal) authentication.getPrincipal();
        var driverEntity = driverLoggedIn.getUser();


        var busAssignment = this.busAssignmentRepository.findByDriverOrThrow(driverEntity);
        var trip = this.tripMapper.toEntity(registerRequest, busAssignment);

        var bus = busAssignment.getBus();
        bus.setActivityStatus(BusActivityStatus.LOADING_PASSENGERS);

        this.busRepository.save(bus); 
        this.tripRepository.save(trip);
    }


    @Override
    public List<ActiveTripResponse> getActiveTrips() {
        var activeTrips = this.tripRepository.findByStatus(TripStatus.IN_PROGRESS);
        return this.tripMapper.toDTO(activeTrips);
    }

    @Override
    public ActiveTripResponse getActiveTrip(UUID tripId) {
        var trip = this.tripRepository.findByIdOrThrow(tripId);
        return this.tripMapper.toDTO(trip);
    }


}
