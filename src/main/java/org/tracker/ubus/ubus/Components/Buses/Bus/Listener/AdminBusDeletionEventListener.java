package org.tracker.ubus.ubus.Components.Buses.Bus.Listener;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Audit.Service.AuditService;
import org.tracker.ubus.ubus.Components.Buses.Bus.Events.AdminBusDeletionAuditEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBusDeletionEventListener {

    private final AuditService auditService;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAdminDeleteBusEvent(AdminBusDeletionAuditEvent event) {
        log.info("Received Admin Bus Deletion Event");

        log.info("Admin Bus Deletion Event running on Thread: {}", Thread.currentThread());


        final var message = event.getMessage();
        this.auditService.save(message, event.getAdmin(), null, event.getAuditType(), event.getCreatedAt());

        log.info("Saved Admin Bus Deletion Event");
    }
}
