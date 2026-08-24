package org.tracker.ubus.ubus.Configuration.WebSocket.Security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Configuration

public class WebSocketSecurityConfig {


    @Bean
    public Map<String, List<User>> connectedStaffAndStudents() {
        return new HashMap<>();
    }


}
