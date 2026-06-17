package org.tracker.ubus.ubus.Components.TokenBlacklist.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.TokenBlacklist.Entity.BlacklistedToken;
import org.tracker.ubus.ubus.Components.TokenBlacklist.Repository.BlacklistedTokenRepository;
import org.tracker.ubus.ubus.Components.TokenBlacklist.Service.Interface.IBlacklistedTokenService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BlacklistedTokenService implements IBlacklistedTokenService {


    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    public void blacklist(String token, long expiresInMilliseconds) {
        BlacklistedToken blacklistedToken = new BlacklistedToken();
        blacklistedToken.setToken(token);
        blacklistedToken.setExpiresAt(LocalDateTime.now().plus(expiresInMilliseconds, ChronoUnit.MILLIS));
        blacklistedTokenRepository.save(blacklistedToken);
    }

    @Override
    public boolean isBlacklisted(String token) {
       if (!blacklistedTokenRepository.existsByToken(token)) {
           return false;
       }
       else{
           BlacklistedToken blacklistedToken = blacklistedTokenRepository.findByToken(token);
           return blacklistedToken.getExpiresAt().isAfter(LocalDateTime.now());
       }
    }

    @Override
    @Scheduled(fixedRate = 300000)//5 min
    public void removeExpiredBlacklistedTokens() {
        blacklistedTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
