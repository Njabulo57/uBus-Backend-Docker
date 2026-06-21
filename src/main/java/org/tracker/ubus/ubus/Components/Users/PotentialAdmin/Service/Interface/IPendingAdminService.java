package org.tracker.ubus.ubus.Components.Users.PotentialAdmin.Service.Interface;

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
}

