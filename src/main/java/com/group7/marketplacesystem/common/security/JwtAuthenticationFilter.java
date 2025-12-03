package com.group7.marketplacesystem.common.security;

import com.group7.marketplacesystem.identity.service.impl.CustomUserDetailsServiceImpl;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


//Nang cap toi uu request thu dung JWT + minimal claims + cache + refresh token
@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtUtils jwtUtils;
    private CustomUserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        String email = null;
        String token = null;

        // Lấy token từ header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                email = jwtUtils.extractEmail(token); // đổi sang extractEmail()
            } catch (JwtException ex) {
                // Token không hợp lệ, để EntryPoint xử lý (401)
            }
        }

        // Nếu email hợp lệ và chưa được xác thực
//        JWT stateless trống mỗi request phải set lại Authentication mỗi request từ token
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email); // dùng email

            if (jwtUtils.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Lưu xác thực vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Cho request đi tiếp
        filterChain.doFilter(request, response);
    }
}
//doFilterInternal(...) là method override của OncePerRequestFilter, chạy một lần cho mỗi request HTTP.
//Nó cho phép bạn can thiệp vào luồng xử lý request, ví dụ:
//        kiểm tra JWT token,
//        xác thực người dùng,
//        rồi cho phép request đi tiếp (filterChain.doFilter()).

//        Từ giờ, mọi nơi trong request này (controller, service) đều có thể gọi:
//        SecurityContextHolder.getContext().getAuthentication()
//        để lấy ra user hiện tại.