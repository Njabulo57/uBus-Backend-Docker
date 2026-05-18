package org.tracker.ubus.ubus.Components.Admin.Listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Admin.Events.DriverApprovedAuditEvent;
import org.tracker.ubus.ubus.Components.Admin.Events.DriverApprovedEmailEvent;
import org.tracker.ubus.ubus.Components.Shared.Mail.EmailService;


@Slf4j
@Component
@RequiredArgsConstructor
public class DriverApprovedEmailListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDriverApprovedEmailNotification(DriverApprovedEmailEvent event) {

        log.info("Driver Approved Email Notification received");
        log.info("Driver Approved Email Notification is now being audited");

        log.info("Driver Approved Email Notification is running on thread {}", Thread.currentThread());

        final var toEmail = event.getUser().getEmail();
        final var subject = event.getHeader();
        final var htmlContent = event.getHeader();
        this.emailService.sendHtmlEmail(toEmail, subject, htmlContent);

        log.info("Driver Approved Email Notification is sent ");
    }
}
