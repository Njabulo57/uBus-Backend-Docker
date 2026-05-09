package org.tracker.ubus.ubus.Components.OneTimePassword.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.User.Entity.User;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table( indexes = {
        @Index(name = "idx_otp_value", columnList = "otp")
})
public class OneTimePassword {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @JoinColumn(nullable = false, unique = true)
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private User user; //we are keeping persist for convenience when persisting


    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

}
