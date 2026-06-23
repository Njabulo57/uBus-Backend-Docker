package org.tracker.ubus.ubus.Components.Audit.Abstract;

import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

public abstract class AbstractAuditingService extends BaseService {

    protected abstract String formatMessage(AuditType auditType, User createdBy, User updatedOn);
}
