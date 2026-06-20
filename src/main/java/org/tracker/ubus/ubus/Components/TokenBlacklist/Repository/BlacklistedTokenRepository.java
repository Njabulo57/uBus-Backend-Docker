package org.tracker.ubus.ubus.Components.TokenBlacklist.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.TokenBlacklist.Entity.BlacklistedToken;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {

    boolean existsByToken(String token);
    void deleteByExpiresAtBefore(LocalDateTime expiresAt);

    BlacklistedToken findByToken(String token);
}
