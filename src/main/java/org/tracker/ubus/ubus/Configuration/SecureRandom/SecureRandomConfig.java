package org.tracker.ubus.ubus.Configuration.SecureRandom;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.security.SecureRandom;

/**
 * Configuration class for providing a SecureRandom instance as a Spring Bean.
 * Useful for cryptographic operations requiring a source of strong randomness.
 */
@Configuration
public class SecureRandomConfig {

    /**
     * Creates and provides a new instance of SecureRandom as a Spring Bean.
     *
     * @return a newly instantiated SecureRandom object for cryptographic operations.
     */
    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
