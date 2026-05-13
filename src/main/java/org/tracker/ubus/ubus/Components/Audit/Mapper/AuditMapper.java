package org.tracker.ubus.ubus.Components.Audit.Mapper;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Audit.Entity.Audit;
import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;
import org.tracker.ubus.ubus.Components.User.Entity.User;

@Component
public class AuditMapper {


    public Audit toEntity(String message, User createdBy, User createdOn, AuditType auditType) {
        return Audit.builder()
                .message(message)
                .type(auditType)
                .createdBy(createdBy)
                .createdOn(createdOn)
                .build();    }
}
