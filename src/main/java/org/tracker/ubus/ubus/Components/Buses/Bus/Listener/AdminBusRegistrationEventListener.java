package org.tracker.ubus.ubus.Components.Buses.Bus.Listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Audit.AuditService;
import org.tracker.ubus.ubus.Components.Buses.Bus.Events.AdminBusRegistrationEvent;


@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBusRegistrationEventListener {

    private final AuditService auditService;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAdminBusRegistrationEvent(AdminBusRegistrationEvent event) {
        log.info("Received Transactional Admin Registration Event");

        log.info("Admin Registration Event running on Thread: {}", Thread.currentThread());

        var message = event.getMessage();
        var auditType = event.getAuditType();
        var admin = event.getAdmin();
        var datetime = event.getCreatedAt();
        this.auditService.save(message, admin, null, auditType, datetime);

        log.info("Saved Admin Registration Event");

    }
}
