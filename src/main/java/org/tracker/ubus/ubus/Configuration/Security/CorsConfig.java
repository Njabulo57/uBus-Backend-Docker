package org.tracker.ubus.ubus.Configuration.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import java.util.Arrays;
import java.util.List;

public class CorsConfig {

    @Bean
    public CorsConfiguration corsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();


        config.setAllowedOrigins(List.of("http://localhost:3000"));

        // Allow HTTP methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow all headers
        config.setAllowedHeaders(List.of("*"));

        // Expose headers
        config.setExposedHeaders(List.of("Authorization"));
        return config;
    }

}
