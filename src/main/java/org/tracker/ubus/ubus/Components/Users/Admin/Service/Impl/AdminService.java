package org.tracker.ubus.ubus.Components.Users.Admin.Service.Impl;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Audit.Abstract.AbstractAuditableService;
import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;
import org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents.AuditEvent;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverActivePage;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverPendingResponseDTO;
import org.tracker.ubus.ubus.Components.Users.Admin.Events.DriverApprovedEmailEvent;
import org.tracker.ubus.ubus.Components.Users.Admin.Mapper.AdminMapper;
import org.tracker.ubus.ubus.Components.Users.Admin.Service.Interface.IAdminService;
import org.tracker.ubus.ubus.Components.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.UUID;

import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole.DRIVER;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus.ACTIVE;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus.ADMIN_APPROVAL_PENDING;

@Service
@RequiredArgsConstructor
public class AdminService extends AbstractAuditableService implements IAdminService {

    private final AdminMapper adminMapper;
    private final UserRepository userRepository;
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
        LocalDateTime now = LocalDateTime.now(); //get the current time for auditing



        final var auditType = AuditType.ADMIN_DRIVER_APPROVAL;
        final var message = this.formatMessage(auditType, admin, driver, now);



        multiEvenPublisher.publish(
                () -> new AuditEvent(this, auditType, admin,
                                            driver, message, now) , //auditing the action
                () -> new DriverApprovedEmailEvent(this, driver)
        );

        return true;
    }

    @Override
    protected String formatMessage(AuditType auditType, User createdBy, User createdOn, LocalDateTime timeStamp) {

        var dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        var formattedTime = timeStamp.format(dateTimeFormatter); //format the date time

        String firstName = createdOn.getFirstname();
        String lastName = createdOn.getLastname();

        String fullName = createdBy.getFirstname() + " " + createdBy.getLastname();
        String[] args = new String[]{ firstName, lastName , fullName, formattedTime};


        return auditType.format(args);
    }
}
