package com.soa.management.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/ws/**").permitAll()
                        .requestMatchers("/api/management/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/bornes/**").hasAnyRole("ADMIN", "OPERATOR", "VIEWER")
                        .anyRequest().authenticated())
                .httpBasic(basic -> {});
        return http.build();
    }
    @Bean
    UserDetailsService users(@Value("${security.admin-username}") String username,
                             @Value("${security.admin-password}") String password,
                             PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername(username).password(encoder.encode(password)).roles("ADMIN").build(),
                User.withUsername("operator").password(encoder.encode("operator")).roles("OPERATOR").build(),
                User.withUsername("viewer").password(encoder.encode("viewer")).roles("VIEWER").build());
    }
    @Bean PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }
}
