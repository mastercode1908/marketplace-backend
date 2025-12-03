package com.group7.marketplacesystem.common.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.group7.marketplacesystem.identity.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//Class CustomUserDetails bạn tự tạo để Spring hiểu “user” của bạn
//UserDetails Mô tả thông tin người dùng (email, password, roles, trạng thái, …)
@AllArgsConstructor
    public class CustomUserDetails implements UserDetails {

    private final User user;
    private final List<String> permissions; // permission code từ RolePermissions

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Nếu role là chuỗi như "Admin" | "Seller" | "Buyer"
//        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Role (ROLE_ prefix)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));

        // Permission chi tiết
        if (permissions != null) {
            permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // Chú ý: dùng email thay vì username nếu bạn login bằng email
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"Banned".equalsIgnoreCase(user.getUserStatus());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
//        return "Active".equalsIgnoreCase(user.getUserStatus());
        // Chỉ login được nếu email đã verify
        return true;
    }
    public boolean isPendingInfo() {
        return "Pending".equalsIgnoreCase(user.getUserStatus());
    }

    // Getter thêm để truy cập entity thật
    public User getUser() {
        return user;
    }

    public Integer getId() {
        return user.getId();
    }

    public String getRole() {
        return user.getRole();
    }
}
