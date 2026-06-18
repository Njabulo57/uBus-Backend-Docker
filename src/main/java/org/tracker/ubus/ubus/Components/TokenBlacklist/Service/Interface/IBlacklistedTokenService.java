package org.tracker.ubus.ubus.Components.TokenBlacklist.Service.Interface;

import org.springframework.scheduling.annotation.Scheduled;

public interface IBlacklistedTokenService {
    public void blacklist(String token, long expiresInMilliseconds);
    public boolean isBlacklisted(String token);

    public void removeExpiredBlacklistedTokens();


}
