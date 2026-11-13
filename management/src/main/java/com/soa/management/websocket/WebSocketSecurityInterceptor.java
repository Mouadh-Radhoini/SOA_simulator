package com.soa.management.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSecurityInterceptor implements ChannelInterceptor {
    private final UserDetailsService users;
    private final PasswordEncoder passwords;
    private final Map<String, Authentication> sessions = new ConcurrentHashMap<>();

    public WebSocketSecurityInterceptor(UserDetailsService users, PasswordEncoder passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Authentication authentication = authenticate(accessor);
            if (authentication == null) throw new IllegalArgumentException("Invalid WebSocket credentials");
            accessor.setUser(authentication);
            if (accessor.getSessionId() != null) {
                sessions.put(accessor.getSessionId(), authentication);
            }
            if (accessor.getSessionAttributes() != null) {
                accessor.getSessionAttributes().put("websocketPrincipal", authentication);
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())
                && accessor.getUser() == null) {
            Authentication authentication = sessions.get(accessor.getSessionId());
            if (authentication != null) {
                accessor.setUser(authentication);
            } else {
                throw new IllegalArgumentException("Authentication is required before subscribing");
            }
        } else if (StompCommand.SEND.equals(accessor.getCommand())) {
            throw new IllegalArgumentException("Client messages are not supported");
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            sessions.remove(accessor.getSessionId());
        }
        return message;
    }

    private Authentication authenticate(StompHeaderAccessor accessor) {
        String login = accessor.getLogin();
        String passcode = accessor.getPasscode();
        if (login == null) login = accessor.getFirstNativeHeader("login");
        if (passcode == null) passcode = accessor.getFirstNativeHeader("passcode");
        if (login == null || passcode == null) {
            String authorization = accessor.getFirstNativeHeader("Authorization");
            if (authorization != null && authorization.startsWith("Basic ")) {
                String decoded = new String(Base64.getDecoder().decode(authorization.substring(6)));
                int separator = decoded.indexOf(':');
                if (separator > 0) {
                    login = decoded.substring(0, separator);
                    passcode = decoded.substring(separator + 1);
                }
            }
        }
        if (login == null || passcode == null) return null;
        try {
            UserDetails user = users.loadUserByUsername(login);
            return passwords.matches(passcode, user.getPassword())
                    ? UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities())
                    : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
