package org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity;


import jakarta.persistence.*;
import lombok.*;

import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.util.*;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusPreference extends TimeAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @JoinColumn(name = "bus_user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Destination fromDestination;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Destination toDestination;



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BusPreference that = (BusPreference) o;
        return Objects.equals(id, that.id) && Objects.equals(user, that.user) && fromDestination == that.fromDestination && toDestination == that.toDestination;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
