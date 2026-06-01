package org.tracker.ubus.ubus.Components.Users.Driver.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Buses.BusRoute.Repository.BusRouteRepository;
import org.tracker.ubus.ubus.Components.Users.Driver.DTO.Response.BusAssignedResponse;
import org.tracker.ubus.ubus.Components.Users.Driver.Mappers.DriverMapper;
import org.tracker.ubus.ubus.Components.Users.Driver.Service.Interface.IDriverService;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;


@Service
@RequiredArgsConstructor
public class DriverService implements IDriverService {


    private final DriverMapper driverMapper;
    private final BusRouteRepository busRouteRepository;
    private final BusAssignmentRepository busAssignmentRepository;



    @Override
    public boolean hasAssignedBus() {

        var authenication = SecurityContextHolder.getContext()
                .getAuthentication();

        var loggedInDriver = (UserPrincipal) authenication.getPrincipal();
        var userEntity = loggedInDriver.getUser();

        return this.busAssignmentRepository.existsByDriver(userEntity);
    }

    @Override
    public BusAssignedResponse getDriverAssignedBus() {

        var authenication = SecurityContextHolder.getContext()
                .getAuthentication();

        var loggedInDriver = (UserPrincipal) authenication.getPrincipal();
        var userEntity = loggedInDriver.getUser();


        var busAssignment = this.busAssignmentRepository.findByDriverOrThrow(userEntity);

        return this.driverMapper.toDTO(busAssignment);
    }
}
