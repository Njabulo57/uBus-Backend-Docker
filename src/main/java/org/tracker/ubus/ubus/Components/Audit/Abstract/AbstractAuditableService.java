package org.tracker.ubus.ubus.Components.Audit.Abstract;

import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.time.LocalDateTime;

/**
 * An abstract service class extending {@code BaseService}, designed to provide
 * functionality for auditing operations within the application. Subclasses
 * are expected to implement the abstract method to handle custom audit
 * message formatting based on specific business needs.
 */
public abstract class AbstractAuditableService extends BaseService {

    /**
     * Formats an audit message based on the provided audit type, user details, and timestamp.
     * This method is intended to be implemented by subclasses to provide custom message formatting
     * that aligns with specific business requirements.
     *
     * @param auditType the type of audit event being logged
     * @param createdBy the user who initiated the operation
     * @param updatedOn the user who last modified the operation
     * @param timeStamp the timestamp representing when the audit event occurred
     * @return a formatted string containing the audit message
     */
    protected abstract String formatMessage(AuditType auditType, User createdBy, User updatedOn, LocalDateTime timeStamp);
}
