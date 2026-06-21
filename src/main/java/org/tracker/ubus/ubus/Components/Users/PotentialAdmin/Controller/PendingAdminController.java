package org.tracker.ubus.ubus.Components.Users.PotentialAdmin.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tracker.ubus.ubus.Components.Users.PotentialAdmin.Service.Interface.IPendingAdminService;

/**
 * Handles HTTP requests related to the management of pending administrator accounts.
 * Provides endpoints for adding, removing, and sending verification emails for potential administrators.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pending-admins")
public class PendingAdminController {


    private final IPendingAdminService pendingAdminService;

    /**
     * Adds the specified email address to the list of pending administrator accounts.
     *
     * @param email the email address of the pending administrator to be added
     * {/pending-admins/add-pending-admin-email}
     * takes a path variable of email.
     */
    @PostMapping("/add-pending-admin-email/{email}")
    public void addPendingEmail(@PathVariable String email) {
        this.pendingAdminService.addPendingAdminEmail(email);
    }


    /**
     * Removes the specified email address from the list of pending administrator accounts.
     *
     * @param email the email address of the pending administrator to be removed
     * {/pending-admins/remove-pending-admin-email}
     * takes a path variable of email.
     */
    @PostMapping("/remove-pending-admin-email/{email}")
    public void removePendingEmail(@PathVariable String email) {
        this.pendingAdminService.removePendingAdminEmail(email);
    }


    /**
     * Sends a verification email to the specified email address as part of the administrator registration process.
     *
     * @param email the email address to which the verification email will be sent
     * {/pending-admins/send-email-verification}
     */
    @PostMapping("/send-email-verification/{email}")
    public void sendEmailVerification(@PathVariable String email) {
        this.pendingAdminService.sendEmailVerification(email);
    }

}
