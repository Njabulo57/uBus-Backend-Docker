package org.tracker.ubus.ubus.Components.Users.PendingAdmin.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.UUID;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PendingAdmin extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User createdBy;


    public PendingAdmin(String email, User createdBy) {
        this.email = email;
        this.createdBy = createdBy;
    }

}
