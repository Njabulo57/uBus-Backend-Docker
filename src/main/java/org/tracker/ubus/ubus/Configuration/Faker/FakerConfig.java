package org.tracker.ubus.ubus.Configuration.Faker;

import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

/**
 * Configuration class for setting up the Faker library with a specific locale.
 * This class defines a bean for the Faker instance to be used across the application.
 * The Faker instance is configured with the locale for English (South Africa).
 */
@Configuration
public class FakerConfig {


    /**
     * Provides a configured instance of the Faker library.
     * The Faker instance is initialized with the locale for English (South Africa).
     *
     * @return a Faker instance configured for the locale en-ZA
     */
    @Bean
    public Faker faker() {
        var locale = new Locale("en", "ZA");
        return new Faker(locale);
    }
}
