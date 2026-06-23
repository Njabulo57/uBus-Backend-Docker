package org.tracker.ubus.ubus.Components.Audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;
import org.tracker.ubus.ubus.Components.Audit.Mapper.AuditMapper;
import org.tracker.ubus.ubus.Components.Audit.Repository.AuditRepository;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditMapper auditMapper;
    private final AuditRepository auditRepository;


    public void save(String message, User createdBy, User createdOn, AuditType auditType, LocalDateTime createdAt) {
        var audit = this.auditMapper.toEntity(message, createdBy, createdOn, auditType, createdAt);
        auditRepository.save(audit);
    }


}
