package org.tracker.ubus.ubus.Components.Users.Admin.Service.Impl;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverActiveResponseDTO;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverPendingResponseDTO;
import org.tracker.ubus.ubus.Components.Users.Admin.Events.DriverApprovedAuditEvent;
import org.tracker.ubus.ubus.Components.Users.Admin.Mapper.AdminMapper;
import org.tracker.ubus.ubus.Components.Users.Admin.Service.Interface.IAdminService;
import org.tracker.ubus.ubus.Components.Audit.Repository.AuditRepository;
import org.tracker.ubus.ubus.Components.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;

import java.util.Collection;
import java.util.UUID;

import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole.DRIVER;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus.ACTIVE;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus.ADMIN_APPROVAL_PENDING;

@Service
@RequiredArgsConstructor
public class AdminService implements IAdminService {

    private final AdminMapper adminMapper;
    private final UserRepository userRepository;
    private final AuditRepository auditRepository;
    private final MultiEvenPublisher multiEvenPublisher;




    @Override
    public Collection<DriverPendingResponseDTO> getPendingDrivers() {

        var pendingDrivers = this.userRepository.findByStatusAndRole(ADMIN_APPROVAL_PENDING, DRIVER);


        return this.adminMapper.toPendingDrivers(pendingDrivers);
    }

    @Override
    public Collection<DriverActiveResponseDTO> getActiveDrivers() {

        var activeAdmins = this.userRepository.findByStatusAndRole(ACTIVE, DRIVER);
        return this.adminMapper.toActiveDrivers(activeAdmins);
    }

    @Override
    @Transactional
    public boolean approveDriver(UUID driverId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var loggedInAdminPrincipal = (UserPrincipal) authentication.getPrincipal(); // getting the admin performing this action
        var adminEntity = loggedInAdminPrincipal.getUser();// only allowed to be called once for security purposes

        var driver = this.userRepository.findByIdOrThrow(driverId);

        if(!driver.getStatus().equals(ADMIN_APPROVAL_PENDING))
            throw new RuntimeException("Driver status is in a Illegal state. Provided: " + driver.getStatus());

        driver.setStatus(ACTIVE);
        this.userRepository.save(driver);


        multiEvenPublisher.publish(
                () -> new DriverApprovedAuditEvent(this,adminEntity, driver) //auditing the action
                //() -> new DriverApprovedEmailEvent(this,driver) // letting the driver know their account is now active
        );

        return true;
    }
}
