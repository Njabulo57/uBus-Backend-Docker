package org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Setter
@Getter
public abstract class AuditEvent extends ApplicationEvent {

    protected final String formattedDate;
    protected final DateTimeFormatter formatter;

    private final AuditType auditType;
    private final LocalDateTime createdAt;

    private String message;


    protected AuditEvent(Object source, AuditType auditType) {
        super(source);
        this.auditType = auditType;
        this.createdAt = LocalDateTime.now();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.formattedDate = LocalDateTime.now().format(formatter);
    }



    public String formatMessage(AuditType auditType, User createdBy, User createdOn, LocalDateTime timeStamp) {

        String firstName = createdOn.getFirstname();
        String lastName = createdOn.getLastname();

        String adminsName = createdBy.getFirstname() + " " + createdBy.getLastname();
        String[] args = new String[]{ firstName, lastName , adminsName, timeStamp.format(formatter)};
        return auditType.format(args);
    }




}
