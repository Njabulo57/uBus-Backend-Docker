package org.tracker.ubus.ubus.Components.Users.Driver.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;
import org.tracker.ubus.ubus.Components.Users.Driver.DTO.Response.BusAssignedResponse;
import org.tracker.ubus.ubus.Components.Users.Driver.Mappers.DriverMapper;
import org.tracker.ubus.ubus.Components.Users.Driver.Service.Interface.IDriverService;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;


@Service
@RequiredArgsConstructor
public class DriverService extends BaseService implements IDriverService {


    private final DriverMapper driverMapper;
    private final BusAssignmentRepository busAssignmentRepository;



    @Override
    public boolean hasAssignedBus() {

        var userEntity = this.getCurrentUser();
        return this.busAssignmentRepository.existsByDriver(userEntity);
    }

    @Override
    public BusAssignedResponse getDriverAssignedBus() {

        var userEntity = this.getCurrentUser();
        var busAssignment = this.busAssignmentRepository.findByDriverOrThrow(userEntity);

        return this.driverMapper.toDTO(busAssignment);
    }
}
