package org.tracker.ubus.ubus.Configuration.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.tracker.ubus.ubus.Components.Jwt.Filter.JwtFilter;
import org.tracker.ubus.ubus.Components.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.User.Repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {

        http = configureEndpointSecurity(http);
        http = configureCsrf(http);
        http = configureSessionManagement(http);
        http = configureJwtFilter(http, jwtFilter);
        return http.build();
    }



    private HttpSecurity configureJwtFilter(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        return http.addFilterBefore(jwtFilter, AuthenticationFilter.class);
    }

    private HttpSecurity configureEndpointSecurity(HttpSecurity http) throws Exception{

        Map<UserRole, String> roles = Stream.of(UserRole.values())
                        .collect(Collectors.toMap( role -> role,
                                UserRole::getLabel
                        ));
        //stores them as ADMIN -> Admin
        http.authorizeHttpRequests(authz ->
                authz
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/one-time-password/**").permitAll()

                    .requestMatchers("/users/**")
                        .hasAnyRole(roles.get(UserRole.ADMIN), roles.get(UserRole.DRIVER),
                            roles.get(UserRole.STAFF), roles.get(UserRole.STUDENT)
                        )

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
    public UserDetailsService userDetailsService(UserRepository userRepository)
            throws UsernameNotFoundException {

        return username -> userRepository.findByEmail(username)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("No active user with email " + username + " Found")
                );
    }
}
