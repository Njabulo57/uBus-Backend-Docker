package org.tracker.ubus.ubus.Components.Users.PendingAdmin.Service.Interface;

import org.tracker.ubus.ubus.Components.Users.PendingAdmin.DTO.Response.PendingAdminResponse;
import java.util.Collection;

public interface IPendingAdminService {

    /**
     * Adds an email to the list of pending administrator accounts.
     *
     * @param email the email address of the pending administrator to be added
     */
    void addPendingAdminEmail(String email);


    /**
     * Removes the specified email address from the list of pending administrator accounts.
     *
     * @param email the email address of the pending administrator to be removed
     */
    void removePendingAdminEmail(String email);


    /**
     * Sends a verification email to the specified email address as part of the administrator registration process.
     *
     * @param email the email address to which the verification email will be sent
     */
    void sendEmailVerification(String email);


    /**
     * Retrieves a list of email addresses that are marked as pending administrator accounts.
     *
     * @return a list of email addresses currently awaiting administrator approval
     */
    Collection<PendingAdminResponse> getPendingAdmins();
}

