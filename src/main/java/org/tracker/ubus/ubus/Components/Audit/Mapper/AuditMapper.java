package org.tracker.ubus.ubus.Components.Audit.Mapper;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Audit.Entity.Audit;
import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.time.LocalDateTime;

@Component
public class AuditMapper {


    public Audit toEntity(String message, User createdBy, User createdOn, AuditType auditType, LocalDateTime createdAt) {
        return Audit.builder()
                .message(message)
                .type(auditType)
                .createdBy(createdBy)
                .createdOn(createdOn)
                .createdAt(createdAt)
                .build();
    }
}
