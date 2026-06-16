package org.tracker.ubus.ubus.Components.ForgotPassword.DTO.Request;

public record ResetPasswordRequest(String newPassword, String newConfirmedPassword,
                                   String otp, String email) {
}
