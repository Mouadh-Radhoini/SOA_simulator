package com.soa.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;

@Configuration
public class SecurityConfiguration {
    @Bean
    ReactiveUserDetailsService users(@Value("${gateway.username:admin}") String username,
                                     @Value("${gateway.password:change-me}") String password,
                                     PasswordEncoder encoder) {
        UserDetails user = User.withUsername(username).password(encoder.encode(password)).roles("ADMIN").build();
        return new MapReactiveUserDetailsService(user);
    }
    @Bean PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }
    @Bean
    SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http.csrf(csrf -> csrf.disable())
                .authorizeExchange(auth -> auth.pathMatchers("/actuator/health/**", "/ws", "/ws/**",
                                "/", "/index.html", "/main.js", "/config.js", "/favicon.ico", "/assets/**").permitAll()
                        .anyExchange().authenticated())
                .httpBasic(basic -> {}).build();
    }
}
