package org.tracker.ubus.ubus.Components.Audit.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Audit.Entity.Audit;
import org.tracker.ubus.ubus.Components.Audit.Enum.AuditType;

import java.util.List;
import java.util.UUID;


@Repository
public interface AuditRepository extends JpaRepository<Audit, UUID> {

    @Query("""
        SELECT audit FROM Audit audit
        LEFT JOIN FETCH audit.createdBy
        LEFT JOIN FETCH audit.createdOn
        ORDER BY audit.createdAt DESC
    """)
    List<Audit> findByType(AuditType  type);
}
