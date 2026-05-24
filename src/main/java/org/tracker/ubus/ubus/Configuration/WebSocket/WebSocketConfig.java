package org.tracker.ubus.ubus.Configuration.WebSocket;


import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {

        //localhost-port-whatever/web-socket
        registry.addEndpoint("/web-socket") // the front end will connect from here
                .setAllowedOriginPatterns(
                        "*"
                ) //which websites are allowed entrance
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {

        registry.enableSimpleBroker("/topic"); //everyone will listen from here
        registry.setApplicationDestinationPrefixes("/app"); //everyone will talk from here

    }
}
