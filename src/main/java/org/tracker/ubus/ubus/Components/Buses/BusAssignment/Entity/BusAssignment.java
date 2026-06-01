package org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Enum.DriverSchedule;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.Objects;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bus_assignment", indexes = {
        @Index(name = "idx_bus_assignment_bus", columnList = "bus_id"),
        @Index(name = "idx_bus_assignment_driver", columnList = "driver_id"),
        @Index(name = "idx_bus_assignment_schedule", columnList = "driver_schedule")
})
public class BusAssignment extends TimeAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User driver;

    @JoinColumn(nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Bus bus;

    @Enumerated(EnumType.STRING)
    private DriverSchedule driverSchedule;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass().equals(o.getClass())) return false;
        BusAssignment that = (BusAssignment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}