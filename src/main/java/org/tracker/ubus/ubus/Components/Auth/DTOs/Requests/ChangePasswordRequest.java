package org.tracker.ubus.ubus.Components.Auth.DTOs.Requests;

/**
 * Represents a request to change a user's password.
 * This class encapsulates the new password that the user wants to set.
 */
public record ChangePasswordRequest(String newPassword) {


}

