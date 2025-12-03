package com.group7.marketplacesystem.common.security;

import com.group7.marketplacesystem.identity.service.impl.CustomUserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private JwtAuthenticationFilter jwtFilter;
    private JwtAuthenticationEntryPoint authEntryPoint;
    private CustomAccessDeniedHandler accessDeniedHandler;
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // .cors(cors -> cors.disable()) // hoặc cors -> {} nếu muốn enable
                // Bật CORS và dùng bean CorsConfigurationSource
                .cors(cors -> cors.configurationSource(corsConfigurationSource)) // dùng bean injected
                .csrf(csrf -> csrf.disable()) // JWT stateless
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // GHN API endpoints công khai (không cần authentication)
                        .requestMatchers("/api/shipping/provinces", "/api/shipping/districts", "/api/shipping/wards",
                                "/api/shipping/calculate-fee")
                        .permitAll()

                        .requestMatchers("/api/admin/notifications/**")
                        .hasAnyAuthority("ROLE_SYSTEMADMIN", "ROLE_CONTENTADMIN") // mapped earlier

                        .requestMatchers("/api/admin/flashsale/**")
                        .hasAnyAuthority("ROLE_SYSTEMADMIN", "ROLE_CONTENTADMIN") // mapped earlier
                        .requestMatchers("/api/admin/category/**")
                        .hasAnyAuthority("ROLE_SYSTEMADMIN", "ROLE_CONTENTADMIN")
                        .requestMatchers("/api/seller/**").hasAuthority("ROLE_SELLER")
                        .requestMatchers("/api/buyer/**").hasAuthority("ROLE_BUYER")
                        .requestMatchers("/api/product/**").permitAll()
                        .requestMatchers("/api/media/**").permitAll()
                        .requestMatchers("/api/category/**").permitAll()
                        .requestMatchers("/api/banners/**").permitAll()
                        .requestMatchers("/api/payment/vnpay/**").permitAll()
                        .requestMatchers("/api/rag/**").permitAll()

                        .anyRequest().authenticated());

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // AuthenticationManager bean (for manual authenticate in controller)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public HttpFirewall vnpayFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedPercent(true);
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowUrlEncodedDoubleSlash(true);
        firewall.setAllowSemicolon(true);

        // Allow all parameter names (VNPay uses strange ones)
        firewall.setAllowedParameterNames(name -> true);

        return firewall;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.httpFirewall(vnpayFirewall());
    }

}
