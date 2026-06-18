package org.tracker.ubus.ubus.Configuration.WebSocket.Security;

import org.springframework.messaging.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManager;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole.*;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;


/**
 * Configuration class for setting up WebSocket security rules and access control.
 *
 * This class leverages Spring Security's messaging framework to define and enforce
 * authorization policies for WebSocket message subscription endpoints. By using
 * message authorization managers, it provides role-based access control to various
 * WebSocket subscription destinations.
 *
 * Annotations:
 * - {@code @Configuration}: Marks this class as a source of Spring bean definitions.
 * - {@code @EnableWebSocketSecurity}: Enables WebSocket security for the application.
 */
@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    AuthorizationManager<Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
                .simpSubscribeDestMatchers("/topic/public", "/topic/all")
                    .permitAll()

                .simpSubscribeDestMatchers("/topic/admin")
                    .hasRole(ADMIN.getLabel())

                .simpSubscribeDestMatchers("/topic/staff")
                    .hasAnyRole(STAFF.getLabel(), ADMIN.getLabel())

                .simpSubscribeDestMatchers("/topic/driver")
                    .hasRole(DRIVER.getLabel())


                .simpSubscribeDestMatchers("/topic/trip/**")
                    .hasAnyRole(STUDENT.getLabel(), STAFF.getLabel(), ADMIN.getLabel())

                .anyMessage().denyAll();

        return messages.build();
    }
}