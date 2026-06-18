package org.tracker.ubus.ubus.Configuration.Faker;

import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class FakerConfig {

    @Bean
    public Faker faker() {
        var locale = new Locale("en", "ZA");
        return new Faker(locale);
    }
}
