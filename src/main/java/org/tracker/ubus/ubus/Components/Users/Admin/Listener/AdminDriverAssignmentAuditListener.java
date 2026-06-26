package org.tracker.ubus.ubus.Components.Users.Admin.Listener;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Users.Admin.Events.AdminDriverAssignmentAuditEvent;
import org.tracker.ubus.ubus.Components.Audit.AuditService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDriverAssignmentAuditListener {

    private final AuditService auditService;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDriverApprovedAuditEvent(AdminDriverAssignmentAuditEvent event) {

        log.info("DriverApprovedAuditEvent received");
        log.info("DriverApprovedAuditEvent is now being audited");
        log.info("DriverApprovedAuditEvent is running on thread {}", Thread.currentThread());
        var message = event.getMessage();
        var createdBy = event.getCreatedBy();
        var createdOn = event.getCreatedOn();
        var auditType = event.getAuditType();
        var eventTime = event.getCreatedAt();
        this.auditService.save(message, createdBy, createdOn, auditType, eventTime);

        log.info("DriverApprovedAuditEvent save successful");
    }
}
