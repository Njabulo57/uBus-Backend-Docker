package org.tracker.ubus.ubus.Components.TokenGenerators.EmailVerificationToken.EmailVerificationTokenService;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Auth.Entity.EmailVerificationToken;
import org.tracker.ubus.ubus.Components.Auth.Exception.External.EmailTokenExpiredException;
import org.tracker.ubus.ubus.Components.Auth.Exception.External.EmailTokenNotFoundException;
import org.tracker.ubus.ubus.Components.Auth.Repository.EmailTokenRepository;

@Component
@RequiredArgsConstructor
public class EmailVerificationTokenService {

    private final EmailTokenRepository emailTokenRepository;

    public String generateEmailToken(UserDetails userDetails) {
        return "NEW EMAIL TOKEN FOR " + userDetails.getUsername() + " role " + userDetails.getAuthorities();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return false;
    }

    public EmailVerificationToken getByToken(String token) {
        return this.emailTokenRepository.findByToken(token)
                .orElseThrow(() -> new EmailTokenNotFoundException("Invalid Token"));
    }

    public void deleteToken(EmailVerificationToken token) {
        this.emailTokenRepository.delete(token);
    }

    @Scheduled(cron = "0 */10 * * * *")
    protected void cleanUpExpiredUnUsedTokens() {

    }

}
