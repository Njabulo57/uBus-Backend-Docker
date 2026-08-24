package org.tracker.ubus.ubus.Configuration.TimeZone;


import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void init() {
        var timeZone = TimeZone.getTimeZone("Africa/Johannesburg");
        TimeZone.setDefault(timeZone);
    }
}
