package com.group7.marketplacesystem.common.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

//Xử lý 401 Unauthorized — tức là chưa đăng nhập / token sai / token hết hạn / không có token.
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res,
                         org.springframework.security.core.AuthenticationException authException) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + authException.getMessage() + "\"}");
    }

//    \"  co nghia la    "
//{
//    "error": "Unauthorized",
//        "message": "Bad credentials"
//}

}
