package org.tracker.ubus.ubus.Components.Users.Admin.Service.Impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverActiveResponseDTO;
import org.tracker.ubus.ubus.Components.Users.Admin.Events.AdminDriverAssignmentAuditEvent;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;

import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverPendingResponseDTO;
import org.tracker.ubus.ubus.Components.Users.Admin.Events.DriverApprovedEmailEvent;
import org.tracker.ubus.ubus.Components.Users.Admin.Mapper.AdminMapper;
import org.tracker.ubus.ubus.Components.Users.Admin.Service.Interface.IAdminService;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.util.Collection;
import java.util.UUID;

import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole.DRIVER;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus.ACTIVE;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus.ADMIN_APPROVAL_PENDING;

@Service
@RequiredArgsConstructor
public class AdminService extends BaseService implements IAdminService {

    private final AdminMapper adminMapper;

    private final BusRepository busRepository;
    private final UserRepository userRepository;
    private final MultiEvenPublisher multiEvenPublisher;


    @Override
    public Collection<DriverPendingResponseDTO> getPendingDrivers() {

        var pendingDrivers = this.userRepository.findByStatusAndRole(ADMIN_APPROVAL_PENDING, DRIVER);
        return this.adminMapper.toPendingDrivers(pendingDrivers);
    }


    @Override
    public Collection<DriverActiveResponseDTO> getActiveDrivers() {

        var activeDriversWithOrWithoutBus = this.busRepository.findAssignedBusesOrDefault();
        return this.adminMapper.toDTOs(activeDriversWithOrWithoutBus);
    }


    @Override
    @Transactional
    public boolean approveDriver(UUID driverId) {

       var admin = this.getCurrentUser();
        var driver = this.userRepository.findByIdOrThrow(driverId);

        if(!driver.getStatus().equals(ADMIN_APPROVAL_PENDING))
            throw new RuntimeException("Driver status is in a Illegal state. Provided: " + driver.getStatus());

        driver.setStatus(ACTIVE);
        this.userRepository.save(driver);

        //publishing the audit and email events
        multiEvenPublisher.publish(
                () -> new AdminDriverAssignmentAuditEvent(this,
                        admin, driver) , //auditing the action
                () -> new DriverApprovedEmailEvent(this, driver)
        );

        return true;
    }

}
