package org.tracker.ubus.ubus.Components.Auth.Listener;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Auth.Events.OtpEmailVerificationEvent;
import org.tracker.ubus.ubus.Components.Shared.Mail.EmailService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OptEmailVerificationListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOtpEmailVerificationEvent(OtpEmailVerificationEvent otpEmailVerificationEvent) {

        log.info("Received Transactional Otp Email Verification Event");

        var savedUser = otpEmailVerificationEvent.getUser();
        var header = otpEmailVerificationEvent.getHeader();
        var body = otpEmailVerificationEvent.getBody();

        log.debug("About to commence the email sending process");
        this.emailService.sendHtmlEmail(savedUser.getEmail(), header, body);

        log.debug("Sent Otp Email Verification Event");
    }

}
