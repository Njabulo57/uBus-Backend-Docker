package org.tracker.ubus.ubus.Components.ForgotPassword.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPassword  extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @JoinColumn(nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token; //this will just be an otp

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
