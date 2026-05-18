package org.tracker.ubus.ubus.Components.Admin.Listener;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Admin.Events.DriverApprovedAuditEvent;
import org.tracker.ubus.ubus.Components.Audit.AuditService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverApprovedAuditListener {

    private final AuditService auditService;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDriverApprovedAuditEvent(DriverApprovedAuditEvent event) {

        log.info("DriverApprovedAuditEvent received");
        log.info("DriverApprovedAuditEvent is now being audited");
        var message = event.getMessage();
        var createdBy = event.getCreatedBy();
        var createdOn = event.getCreatedOn();
        var auditType = event.getAuditType();
        this.auditService.save(message, createdBy, createdOn, auditType);

        log.info("DriverApprovedAuditEvent save successful");
    }
}
