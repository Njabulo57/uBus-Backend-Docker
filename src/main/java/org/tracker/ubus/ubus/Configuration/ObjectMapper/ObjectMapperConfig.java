package org.tracker.ubus.ubus.Configuration.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

/**
 * Configuration class for setting up an {@link ObjectMapper} Bean.
 * This class provides a centralized configuration for creating and
 * customizing the {@link ObjectMapper} instance used throughout the application.
 */
@Configuration
public class ObjectMapperConfig {

    /**
     * Creates and provides a singleton {@link ObjectMapper} Bean.
     * This method is used to configure and return an {@link ObjectMapper} instance
     * which can be utilized for JSON processing across the application.
     *
     * @return an instance of {@link ObjectMapper} for JSON data binding and manipulation
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}