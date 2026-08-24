package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.MaintenanceIssue;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.Priority;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;

import java.time.LocalDate;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;



@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "idx_bus_operational_history_date", columnList = "date_operated"),
        @Index(name = "idx_bus_operational_history_bus", columnList = "bus_id"),
        @Index(name = "idx_bus_op", columnList = "date_operated, bus_id"),
        @Index(name = "idx_bus_op_status", columnList = "bus_operational_status"),
        @Index(name = "idx_bus_op_issue", columnList = "maintenance_issue"),
        @Index(name = "idx_bus_Priority", columnList = "priority")
})
public class BusOperationalHistory {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = LAZY, optional = false)
    private Bus bus;


    @Enumerated(EnumType.STRING)
    private MaintenanceIssue maintenanceIssue;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private BusOperationalStatus busOperationalStatus;

    private String description;

    @Column(nullable = false)
    private LocalDate dateOperated;

    private LocalDate dateResolved;
}
