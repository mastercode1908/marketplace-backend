package com.group7.marketplacesystem.common.security;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        // 0. Debug logs
        System.err.println("JwtHandshakeInterceptor: " + request.getURI());

        // 1. Allow SockJS info request
        if (request.getURI().getPath().endsWith("/info")) {
            return true;
        }

        // 2. Try getting token from Header
        List<String> auth = request.getHeaders().get("Authorization");
        if (auth != null && !auth.isEmpty() && auth.get(0).startsWith("Bearer ")) {
            String token = auth.get(0).substring(7);
            attributes.put("jwt", token);
            return true;
        }

        // 3. Try getting token from Query Param (for SockJS)
        if (request instanceof org.springframework.http.server.ServletServerHttpRequest) {
            org.springframework.http.server.ServletServerHttpRequest servletRequest = (org.springframework.http.server.ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token == null) token = servletRequest.getServletRequest().getParameter("access_token");
            
            if (token != null && !token.isEmpty()) {
                attributes.put("jwt", token);
                return true;
            }
        }
        
        // 4. Fallback manual parsing (just in case)
        String query = request.getURI().getQuery();
        if (query != null) {
             if (query.contains("token=") || query.contains("access_token=")) {
                 // Simple extraction to avoid complex parsing issues
                 String[] parts = query.split("token=");
                 if (parts.length > 1) {
                     String token = parts[1].split("&")[0];
                     attributes.put("jwt", token);
                     return true;
                 }
             }
        }

        System.err.println("JwtHandshakeInterceptor: Unauthorized - No token found");
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Không cần làm gì
    }
}
