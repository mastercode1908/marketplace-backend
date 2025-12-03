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

        List<String> authHeaders = request.getHeaders().get("Authorization");

        if (authHeaders != null && !authHeaders.isEmpty()) {
            String token = authHeaders.get(0).replace("Bearer ", "");
            attributes.put("jwt", token);  // Lưu token vào WebSocket session
            return true;
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false; // không có token => 401
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Không cần làm gì
    }
}
