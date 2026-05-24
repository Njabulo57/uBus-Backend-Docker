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
public class Bus extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name; //eg APK_DFC_1

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BusType type; //electric, combustion

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BusOperationalStatus operationalStatus; // if on service, out of service or being maintained


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BusActivityStatus  activityStatus;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;


    @OneToMany(mappedBy = "bus", fetch = FetchType.LAZY)
    private final Set<BusAssignment> busAssignments = new HashSet<>();

    @OneToOne(mappedBy = "bus")
    private BusRoute busRoute;


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Bus bus = (Bus) o;
        return getCapacity() == bus.getCapacity() &&
                isActive() == bus.isActive() &&
                Objects.equals(getId(), bus.getId()) &&
                Objects.equals(getName(), bus.getName()) &&
                Objects.equals(getModel(), bus.getModel()) &&
                getType() == bus.getType() &&
                getOperationalStatus() == bus.getOperationalStatus() &&
                getActivityStatus() == bus.getActivityStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getModel(),
                getCapacity(), getType(), getOperationalStatus(),
                getActivityStatus(), isActive());
    }

}
