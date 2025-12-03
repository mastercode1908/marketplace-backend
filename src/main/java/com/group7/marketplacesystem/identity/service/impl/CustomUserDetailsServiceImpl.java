package com.group7.marketplacesystem.identity.service.impl;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.common.security.CustomUserDetails;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.RolePermissionRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

//    Dùng cho Spring Security load user từ DB để xác thực.
@Service
@AllArgsConstructor
public class CustomUserDetailsServiceImpl  implements UserDetailsService {
    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Map DB role to authority string, prefix ROLE_ để phục vụ .hasRole("ADMIN") nếu muốn
//        String dbRole = user.getRole(); // e.g. "SystemAdmin"
//        String authority = "ROLE_" + dbRole.toUpperCase(); // ROLE_SYSTEMADMIN
        List<String> permissions = rolePermissionRepository.findPermissionCodesByRole(user.getRole().toUpperCase());

        //C1
//        return org.springframework.security.core.userdetails.User
//                .withUsername(user.getEmail())
//                .password(user.getPassword())
//                .authorities(new SimpleGrantedAuthority(authority))
//                .accountExpired(false)
//                .accountLocked(false)
//                .credentialsExpired(false)
//                .disabled(!"Active".equalsIgnoreCase(user.getUserStatus()))
//                .build();

        //C2
//        return new CustomUserDetails(user); // trả về CustomUserDetails
        return new CustomUserDetails(user, permissions);

    }

}
