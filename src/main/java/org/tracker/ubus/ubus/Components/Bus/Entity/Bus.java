package org.tracker.ubus.ubus.Components.Bus.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusType;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;

import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
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

}
