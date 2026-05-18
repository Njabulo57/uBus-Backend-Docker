package org.tracker.ubus.ubus.Components.Auth.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Auth.Entity.EmailVerificationToken;
import org.tracker.ubus.ubus.Components.User.Entity.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {


    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByUser(User user);
}
