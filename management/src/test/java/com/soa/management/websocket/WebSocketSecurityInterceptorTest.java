package com.soa.management.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class WebSocketSecurityInterceptorTest {
    private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final UserDetailsService users = username -> User.withUsername(username)
            .password(encoder.encode("secret")).roles("VIEWER").build();
    private final WebSocketSecurityInterceptor interceptor = new WebSocketSecurityInterceptor(users, encoder);

    @Test
    void authenticatesStompConnectHeaders() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("login", "viewer");
        accessor.addNativeHeader("passcode", "secret");
        Message<?> result = interceptor.preSend(MessageBuilder.withPayload(new byte[0]).setHeaders(accessor).build(), null);
        assertNotNull(result);
    }

    @Test
    void rejectsInvalidCredentials() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("login", "viewer");
        accessor.addNativeHeader("passcode", "wrong");
        Message<?> message = MessageBuilder.withPayload(new byte[0]).setHeaders(accessor).build();
        assertThrows(IllegalArgumentException.class, () -> interceptor.preSend(message, null));
    }
}
