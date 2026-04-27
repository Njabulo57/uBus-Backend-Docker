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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.tracker.ubus.ubus.Components.Jwt.Filter.JwtFilter;
import org.tracker.ubus.ubus.Components.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.User.Repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.tracker.ubus.ubus.Components.User.Enum.UserRole.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtFilter jwtFilter,
                                                   CorsConfigurationSource configurationSource)
            throws Exception {

        http = configureEndpointSecurity(http);
        http = configureCsrf(http);
        http = configureSessionManagement(http);
        http = configureJwtFilter(http, jwtFilter);
        http = configureCors(http, configurationSource);
        return http.build();
    }



    private HttpSecurity configureJwtFilter(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        return http.addFilterBefore(jwtFilter, AuthenticationFilter.class);
    }

    private HttpSecurity configureEndpointSecurity(HttpSecurity http) throws Exception{

        http.authorizeHttpRequests(authz ->
                authz
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/one-time-password/**").permitAll()

                    .requestMatchers("/users/**")
                        .hasAnyRole(ADMIN.getLabel(), DRIVER.getLabel(),
                            STAFF.getLabel(), STUDENT.getLabel()
                        )

                    .requestMatchers("/busses/register")
                        .hasRole(ADMIN.getLabel())

                    .requestMatchers("/busses/**")
                        .hasRole(ADMIN.getLabel())

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

    private HttpSecurity configureCors(HttpSecurity http, CorsConfigurationSource configuration) {
        return http.cors(cors ->
                cors.configurationSource(configuration));
    }


}
