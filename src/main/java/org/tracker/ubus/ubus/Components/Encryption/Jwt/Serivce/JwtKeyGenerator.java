package org.tracker.ubus.ubus.Components.Encryption.Jwt.Serivce;

import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Encryption.Abstract.ISecretKeyGenerator;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtKeyGenerator implements ISecretKeyGenerator {



    @Override
    public SecretKey generateSecretKey(String key) {
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);  // Converts to bytes
        return Keys.hmacShaKeyFor(bytes);  // Wraps bytes as a SecretKey object
    }
}
