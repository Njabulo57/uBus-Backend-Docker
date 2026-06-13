package org.tracker.ubus.ubus.Configuration.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.tracker.ubus.ubus.Components.TokenGenerators.Jwt.Filter.JwtFilter;

import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole.*;

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

    private HttpSecurity configureEndpointSecurity(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(authz ->
                authz
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/one-time-password/**").permitAll()


                    .requestMatchers("/users/**")
                        .hasAnyRole(ADMIN.getLabel(), DRIVER.getLabel(),
                            STAFF.getLabel(), STUDENT.getLabel())

                    .requestMatchers("/drivers/**")
                        .hasRole(DRIVER.getLabel())

                    .requestMatchers("/busses/**")
                        .hasRole(ADMIN.getLabel())

                    .requestMatchers("/trips/register-bus-trip")
                        .hasRole(ADMIN.getLabel())


                    .requestMatchers("/trips/get-active-trips").permitAll()
                    .requestMatchers("/trips/get-trip/").permitAll()

                    .requestMatchers("/web-socket/**").permitAll()
                    .requestMatchers("/web-socket").permitAll()

                    .requestMatchers("/admins/**")
                        .hasRole(ADMIN.getLabel())


                        .requestMatchers("/trips-data/**")
                        .permitAll()

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
