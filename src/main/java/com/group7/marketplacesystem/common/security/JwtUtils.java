package com.group7.marketplacesystem.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private final Key key;
    private final long ACCESS_EXP = 2 * 60 * 60 * 1000L;          // 1 giờ
    private final long REFRESH_EXP = 7 * 24 * 60 * 60 * 1000L; // 7 ngày


    public JwtUtils(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }
//    public JwtUtils() {
//        // Thực tế nên load từ ENV hoặc cấu hình, không hardcode
//        String secret = System.getenv().getOrDefault(
//                "JWT_SECRET",
//                "super-secret-jwt-key-change-this-to-long-secure-value-12345678901234567890"
//        );
//        this.key = Keys.hmacShaKeyFor(secret.getBytes());
//    }

    // =====================================================================
    // GENERATE TOKEN
    // =====================================================================

    //    Khi bạn đã load user từ DB (login xong)
    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), getRoles(userDetails), ACCESS_EXP);
    }


    //    Khi bạn đã load user từ DB (login xong)
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), getRoles(userDetails), REFRESH_EXP);
    }

    // =====================================================================
    // BUILD TOKEN
    // =====================================================================

    //    getRoles() trích xuất danh sách role của user:
    private List<String> getRoles(UserDetails userDetails) {
        return userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    private String buildToken(String subject, List<String> roles, long expMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expMs);

        return Jwts.builder()
                .setSubject(subject)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // =====================================================================
    // VALIDATION
    // =====================================================================
//    Xác thực token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // overload cho filter (kiểm tra token + userDetails)
    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }


    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    // =====================================================================
    // EXTRACTION
    // =====================================================================

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        Object roles = extractAllClaims(token).get("roles");
        if (roles instanceof List<?>) {
            return ((List<?>) roles).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    // Lấy role đầu tiên từ token (ví dụ ROLE_SELLER, ROLE_BUYER, ...)
    public String extractRole(String token) {
        List<String> roles = extractRoles(token);
        if (roles.isEmpty()) {
            return null;
        }
        // Bỏ prefix ROLE_ nếu muốn lấy đúng tên gốc như "SELLER"
        String role = roles.get(0);
        return role.startsWith("ROLE_") ? role.substring(5) : role;
    }


    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //C2 buildToken, doBuildToken dung Map (2 ham duoi nay bang 1 ham build Token tren kia sd List)
    //        private String buildToken(UserDetails userDetails, long expirationMs) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("roles", userDetails.getAuthorities()
//                .stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList()));
//
//        return doBuildToken(userDetails.getUsername(), claims, expirationMs);
//    }
//    private String doBuildToken(String subject, Map<String, Object> claims, long expirationMs) {
//        Date now = new Date();
//        Date exp = new Date(now.getTime() + expirationMs);
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(subject) // subject = email
//                .setIssuedAt(now)
//                .setExpiration(exp)
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }


}
