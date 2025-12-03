package com.group7.marketplacesystem.common.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class JwtChatHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        // 1. Try getting token from Header
        List<String> auth = request.getHeaders().get("Authorization");
        if (auth != null && !auth.isEmpty() && auth.get(0).startsWith("Bearer ")) {
            String token = auth.get(0).substring(7);
            attributes.put("jwt", token);
            return true;
        }

        // 2. Try getting token from Query Param (for SockJS)
        if (request instanceof org.springframework.http.server.ServletServerHttpRequest) {
            org.springframework.http.server.ServletServerHttpRequest servletRequest = (org.springframework.http.server.ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token != null && !token.isEmpty()) {
                attributes.put("jwt", token);
                return true;
            }
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
    }
}
