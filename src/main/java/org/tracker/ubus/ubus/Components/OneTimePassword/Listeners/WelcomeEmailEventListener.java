package org.tracker.ubus.ubus.Components.OneTimePassword.Listeners;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.OneTimePassword.Events.WelcomeEmailEvent;
import org.tracker.ubus.ubus.Components.Shared.Mail.EmailService;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelcomeEmailEventListener {

    private final EmailService emailService;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWelcomeEmailEvent(WelcomeEmailEvent event) {
        log.info("Received Transactional Welcome Email Event");
        log.info("About to commence the email sending process on Thread: {}", Thread.currentThread());

        var toEmail = event.getToEmail();
        var header = event.getHeader();
        var body = event.getBody();
        this.emailService.sendHtmlEmail(toEmail, header, body);
        log.info("Sent Welcome Email Event");
    }
}
