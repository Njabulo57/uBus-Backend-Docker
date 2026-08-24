package org.tracker.ubus.ubus.Components.Users.Admin.Listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Shared.Mail.EmailService;
import org.tracker.ubus.ubus.Components.Users.Admin.Events.DriverReceivedEmailEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverRegistrationSuccessfulEventListener {

    private final EmailService emailService;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDriverRegistrationSuccessful(DriverReceivedEmailEvent event) {
        log.info("Received Transactional Driver Registration Successful Event");

        var toEmail = event.getToEmail();
        var header = event.getHeader();
        var body = event.getBody();
        log.info("Sending Driver Registration Successful Event on thread: {}", Thread.currentThread());
        this.emailService.sendHtmlEmail(toEmail, header, body);
    }
}
