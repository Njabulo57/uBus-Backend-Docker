package org.tracker.ubus.ubus.Components.Users.PendingAdmin.Mapper;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Users.PendingAdmin.DTO.Response.PendingAdminResponse;
import org.tracker.ubus.ubus.Components.Users.PendingAdmin.Entity.PendingAdmin;
import java.util.List;

@Component
public class PendingAdminMapper {


    public List<PendingAdminResponse> toDTO(List<PendingAdmin> pendingAdmins) {
        return pendingAdmins.stream()
                .map(this::toDTO)
                .toList();
    }


    private PendingAdminResponse toDTO(PendingAdmin pendingAdmin) {
        return PendingAdminResponse.builder()
                .email(pendingAdmin.getEmail())
                .createdAt(pendingAdmin.getCreatedAt())
                .IsInviteSent(pendingAdmin.isEmailSent())
                .build();
    }
}
