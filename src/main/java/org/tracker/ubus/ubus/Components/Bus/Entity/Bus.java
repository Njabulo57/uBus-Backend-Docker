package org.tracker.ubus.ubus.Components.Bus.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusType;
import org.tracker.ubus.ubus.Components.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.BusRoute.Entity.BusRoute;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bus", indexes = {
        @Index(name = "idx_bus_is_active", columnList = "isActive"),
        @Index(name = "idx_bus_name", columnList = "name"),
        @Index(name = "idx_bus_operational_status", columnList = "operationalStatus"),
        @Index(name = "idx_bus_activity_status", columnList = "activityStatus"),
        @Index(name = "idx_bus_type", columnList = "type")
})
public class Bus extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BusType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BusOperationalStatus operationalStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BusActivityStatus activityStatus;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "bus", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<BusAssignment> busAssignments = new HashSet<>();

    @OneToOne(mappedBy = "bus")
    private BusRoute busRoute;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Bus bus = (Bus) o;
        return Objects.equals(id, bus.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}