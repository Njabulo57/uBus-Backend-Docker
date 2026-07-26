package org.tracker.ubus.ubus.Components.Trips.TripUser.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Enum.TripUserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;


import java.util.UUID;

import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole.STAFF;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole.STUDENT;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "trip_id"}))
public class TripUser extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @JoinColumn(nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @JoinColumn(nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Trip trip;

    @JoinColumn(nullable = false, updatable = false)
    private TripUserStatus status = TripUserStatus.IN_BUS;

    protected void validateUserRole() {
        var role = this.user.getRole();
        if(!(role == STAFF) && !(role == STUDENT))
            throw new IllegalStateException("User is not a " + this.user.getRole().name() );
    }
    @JoinColumn(nullable = false, updatable = false)
    private boolean isFirstTrip = true;

}
