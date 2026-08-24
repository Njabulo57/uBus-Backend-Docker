package org.tracker.ubus.ubus.Components.Buses.Bus.Listener;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Buses.Bus.Events.AdminBusAssignmentRemovalEvent;
import org.tracker.ubus.ubus.Components.Shared.Mail.EmailService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBusAssignmentRemovalListener {

    private final EmailService emailService;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBusUnassignmentEmailSender(AdminBusAssignmentRemovalEvent event) {

        var drivers = event.getDriversAssigned();
        var header = event.getHeader();

        //sending both drivers the update without hindering the admin
        drivers.forEach(driver -> {
            var driverEmail = event.getBodyWithDriver(driver);

            log.info("Sending email to driver: {} on Thread {}", driver.getEmail(), Thread.currentThread());
            this.emailService.sendHtmlEmail(driver.getEmail(), header, driverEmail);
        });

    }
}
