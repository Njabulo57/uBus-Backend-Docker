package org.tracker.ubus.ubus.Components.Users.Admin.Service.Impl;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Audit.Abstract.AbstractAuditingService;
import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;
import org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents.AuditEvent;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverActivePage;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverPendingResponseDTO;
import org.tracker.ubus.ubus.Components.Users.Admin.Events.DriverApprovedEmailEvent;
import org.tracker.ubus.ubus.Components.Users.Admin.Mapper.AdminMapper;
import org.tracker.ubus.ubus.Components.Users.Admin.Service.Interface.IAdminService;
import org.tracker.ubus.ubus.Components.Audit.Repository.AuditRepository;
import org.tracker.ubus.ubus.Components.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;

import java.util.Collection;
import java.util.UUID;

import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole.DRIVER;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus.ACTIVE;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus.ADMIN_APPROVAL_PENDING;

@Service
@RequiredArgsConstructor
public class AdminService extends AbstractAuditingService implements IAdminService {

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
    public DriverActivePage getActiveDrivers(Pageable pageable) {

        var pageOfDrivers = this.userRepository.findByStatusAndRole(ACTIVE, DRIVER, pageable);
        return this.adminMapper.toDTO(pageOfDrivers);
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


        final var auditType = AuditType.ADMIN_DRIVER_APPROVAL;
        final var message = this.formatMessage(auditType, admin, driver);

        multiEvenPublisher.publish(
                () -> new AuditEvent(this, auditType, admin, driver, message), //auditing the action
                () -> new DriverApprovedEmailEvent(this, driver)
        );

        return true;
    }

    @Override
    protected String formatMessage(AuditType auditType, User createdBy, User createdOn) {

        String firstName = createdOn.getFirstname();
        String lastName = createdOn.getLastname();

        String fullName = createdOn.getFirstname() + " " + createdOn.getLastname();
        String[] args = new String[]{ fullName, lastName , fullName};
        return auditType.format(args);
    }
}
