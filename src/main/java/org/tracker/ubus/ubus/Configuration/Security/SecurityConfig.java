package org.tracker.ubus.ubus.Configuration.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.tracker.ubus.ubus.Components.User.Repository.UserRepository;

@Configuration
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http = configureEndpointSecurity(http);
        http = configureCsrf(http);
        http = configureSessionManagement(http);
        return http.build();
    }



    private HttpSecurity configureEndpointSecurity(HttpSecurity http) throws Exception{
         http.authorizeHttpRequests(authz ->
                authz
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
        );
        return http;
    }

    private HttpSecurity configureCsrf(HttpSecurity http) throws Exception{
        http.csrf(AbstractHttpConfigurer::disable);
        return http;
    }

    private HttpSecurity configureSessionManagement(HttpSecurity http) throws Exception{
        http.sessionManagement(sessionManagement ->
            sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
        return http;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) throws Exception{

        return username -> userRepository.findByEmail(username)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("User With Email " + username + " Does Not Exist")
                );
    }
}
