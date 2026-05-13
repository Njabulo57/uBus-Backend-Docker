package org.tracker.ubus.ubus.Components.Audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;
import org.tracker.ubus.ubus.Components.Audit.Mapper.AuditMapper;
import org.tracker.ubus.ubus.Components.Audit.Repository.AuditRepository;
import org.tracker.ubus.ubus.Components.User.Entity.User;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditMapper auditMapper;
    private final AuditRepository auditRepository;


    public void save(String message, User createdBy, User createdOn, AuditType auditType) {
        var audit = this.auditMapper.toEntity(message, createdBy, createdOn, auditType);
        auditRepository.save(audit);
    }


}
