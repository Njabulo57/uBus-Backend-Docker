package org.tracker.ubus.ubus.Components.Auth.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Auth.Exception.Internal.UserRoleNotAllowedException;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class EmailVerificationToken extends TimeAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @JoinColumn( nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    private User user;

    @Column(nullable = false)
    private boolean isVerified;

    private LocalDateTime expiresAt;

    public EmailVerificationToken(String token, UUID id, User user)
            throws UserRoleNotAllowedException {
        validateRoles();
        this.token = token;
        this.id = id;
        this.user = user;
    }


    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    private void validateRoles() {
        if(user.getRole() != UserRole.STAFF  &&
                user.getRole() != UserRole.DRIVER)
            throw new UserRoleNotAllowedException("User Role Not Permitted For Email Token Verification");
    }


}