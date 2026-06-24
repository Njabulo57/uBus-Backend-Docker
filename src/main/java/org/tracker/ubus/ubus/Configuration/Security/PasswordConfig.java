package org.tracker.ubus.ubus.Configuration.Security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * Configuration class for password-related settings.
 * This class provides a bean for password encoding, which is essential for securely handling
 * user credentials in the application.
 *
 * The password encoder is configured to use {@link BCryptPasswordEncoder} with a specific strength parameter.
 * This configuration ensures strong password hashing, making it resistant to brute-force attacks.
 */
@Configuration
public class PasswordConfig {

    /**
     * Creates and configures a {@link PasswordEncoder} bean using {@link BCryptPasswordEncoder}.
     * This encoder is designed for securely hashing passwords with a specified strength factor.
     *
     * @return a configured {@link PasswordEncoder} instance for hashing passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
