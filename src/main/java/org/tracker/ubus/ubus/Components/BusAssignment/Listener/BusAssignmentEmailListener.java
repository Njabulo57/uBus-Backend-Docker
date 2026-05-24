package org.tracker.ubus.ubus.Components.BusAssignment.Listener;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.BusAssignment.Events.BusAssignmentEmailNotificationEvent;
import org.tracker.ubus.ubus.Components.Shared.Mail.EmailService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusAssignmentEmailListener {

    private final EmailService emailService;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBusAssignmentEmailEvent(BusAssignmentEmailNotificationEvent event) {

        log.info("BusAssignmentEmailListener received");


        var body = event.getBody();
        var header = event.getHeader();
        var subject = event.getSubject();

        log.info("Attempting to send an email");
        emailService.sendHtmlEmail(subject, body, header);

        log.info("Email sent");
    }

}
