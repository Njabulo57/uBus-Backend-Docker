package org.tracker.ubus.ubus.Components.Users.PotentialAdmin.Listeners;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Shared.Mail.EmailService;
import org.tracker.ubus.ubus.Components.Users.PotentialAdmin.Events.PendingAdminAdditionEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingAdminAdditionListener {

    private final EmailService emailService;

    @Async
    public void handlePendingAdminAdditionEvent(PendingAdminAdditionEvent event) {

        log.info("Pending Admin Addition Event received");
        var toEmail = event.getToEmail();
        var body = event.getHtmlBody();
        var header = event.getHeader();

        log.info("Sending Pending Admin Addition Event on thread: {}", Thread.currentThread());
        this.emailService.sendHtmlEmail(toEmail, header, body);

        log.info("Pending Admin Addition Event sent");

    }
}
