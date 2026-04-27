package org.tracker.ubus.ubus.Configuration.Security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.tracker.ubus.ubus.Components.User.Repository.UserRepository;

@Configuration
public class UserDetailsServiceConfig {

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository)
            throws UsernameNotFoundException {

        return username -> userRepository.findByEmail(username)
                .map(UserPrincipal::new)
                .orElseThrow(() ->
                        new UsernameNotFoundException("No active user with email " + username + " Found")
                );
    }
}
