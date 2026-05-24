package org.tracker.ubus.ubus.Components.BusAssignment.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.BusAssignment.Enum.DriverSchedule;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.User.Entity.User;

import java.util.Objects;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusAssignment extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false,  unique = true)
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
        if (o == null || getClass() != o.getClass()) return false;
        BusAssignment that = (BusAssignment) o;
        return Objects.equals(id, that.id) && driverSchedule == that.driverSchedule;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, driverSchedule);
    }
}
