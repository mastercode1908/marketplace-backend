package com.group7.marketplacesystem.common.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

//    (nếu muốn bắt lỗi 403 riêng)
//Xử lý 403 Forbidden — tức là đã đăng nhập nhưng không đủ quyền (role).
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        res.setContentType("application/json");
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        res.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"" + ex.getMessage() + "\"}");
    }

}

