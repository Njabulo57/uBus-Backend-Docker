package org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Getter
public abstract class AuditEvent extends ApplicationEvent {

    private final AuditType auditType;
    private final User createdBy;
    private final User createdOn;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String message;
    public AuditEvent(Object source, AuditType auditType, User createdBy, User createdOn) {
        super(source);
        this.auditType = auditType;
        this.createdBy = createdBy;
        this.createdOn = createdOn;

        String formattedDateTime = LocalDateTime.now().format(formatter);
        this.message = auditType.adminDriverApprovalMsg(createdBy, createdOn, formattedDateTime);
    }



}
