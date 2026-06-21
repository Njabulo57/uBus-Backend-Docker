package org.tracker.ubus.ubus.Components.Users.PendingAdmin.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Users.PendingAdmin.Service.Interface.IPendingAdminService;

import java.util.Collection;

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
     * Retrieves a collection of email addresses that are marked as pending administrator accounts.
     *
     * @return a collection of email addresses currently awaiting administrator approval
     */
    @GetMapping("/get-pending-admins")
    public Collection<String> getPendingAdmins() {
        return this.pendingAdminService.getPendingAdmins();
    }


    /**
     * Removes the specified email address from the list of pending administrator accounts.
     *
     * @param email the email address of the pending administrator to be removed
     * {/pending-admins/remove-pending-admin-email}
     * takes a path variable of email.
     */
    @DeleteMapping("/remove-pending-admin-email/{email}")
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
