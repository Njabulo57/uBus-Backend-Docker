package org.tracker.ubus.ubus.Components.Users.PendingAdmin.DTO.Response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PendingAdminResponse(String email, LocalDateTime createdAt, boolean IsInviteSent) {
}
