package com.group7.marketplacesystem.common.config;

import com.group7.marketplacesystem.common.security.JwtChatHandshakeInterceptor;
import com.group7.marketplacesystem.common.security.JwtHandshakeInterceptor;
import com.group7.marketplacesystem.common.utils.JwtUserUtil;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.group7.marketplacesystem.identity.service.impl.CustomUserDetailsServiceImpl;
import com.group7.marketplacesystem.common.security.JwtUtils;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@AllArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final JwtChatHandshakeInterceptor jwtChatHandshakeInterceptor;
    private final JwtUserUtil jwtUserUtil; // <-- thêm đây

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                                                      WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {
                        String token = (String) attributes.get("jwt");
                        if (token != null) {
                            Integer userId = jwtUserUtil.extractUserId(token);
                            return () -> userId.toString();
                        }
                        return null;
                    }
                })
                .withSockJS();

        // Socket mới dành riêng cho chat
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtChatHandshakeInterceptor)
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                                                      WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {
                        String token = (String) attributes.get("jwt");
                        if (token != null) {
                            Integer userId = jwtUserUtil.extractUserId(token);
                            return () -> userId.toString(); // Principal.getName() = userId
                        }
                        return null;
                    }
                })
                .withSockJS();
    }
}
