package org.tracker.ubus.ubus.Components.BusPreference.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import org.tracker.ubus.ubus.Components.User.Enum.UserBusPreference;

import java.util.UUID;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusPreference extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @JoinColumn(nullable = false, updatable = false)
    @OneToOne(cascade = CascadeType.PERSIST,  fetch = FetchType.LAZY)
    private User busUser;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserBusPreference userBusPreference;
}
