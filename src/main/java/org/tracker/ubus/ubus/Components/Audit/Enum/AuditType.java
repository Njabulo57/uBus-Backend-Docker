package org.tracker.ubus.ubus.Components.Audit.Enum;


import lombok.AllArgsConstructor;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
public enum AuditType {


    ADMIN_DRIVER_APPROVAL("Driver %s %s account approved by %s %s on %s"),
    STUDENT_EMAIL_APPROVAL("");


    private final String message;


    public String getConstructedMessage(User createdBy, User createdOn, String formattedDateTime) {
        String creatorFistName = createdBy.getFirstname();
        String creatorLastName = createdBy.getLastname();

        String createdOnFistName = createdOn != null ?  createdOn.getFirstname() : "";
        String createdOnLastName = createdOn != null ?  createdOn.getLastname() : "";

        return String.format(message,
                createdOnFistName,
                createdOnLastName,

                creatorFistName,
                creatorLastName,

                formattedDateTime);
    }
}
