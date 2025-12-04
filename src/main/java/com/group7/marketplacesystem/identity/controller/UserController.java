package com.group7.marketplacesystem.identity.controller;

import com.group7.marketplacesystem.identity.dto.response.UserResponse;
import com.group7.marketplacesystem.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.group7.marketplacesystem.identity.dto.response.UserDetailResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String userStatus,
            Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsers(username, email, phone, role, userStatus, pageable));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<UserDetailResponse> getUserDetails(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getUserDetails(userId));
    }

    @PutMapping("/{userId}/userStatus={status}")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable Integer userId,
                                                         @PathVariable String status) {
        return ResponseEntity.ok(userService.updateUserStatus(userId, status));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'CONTENTADMIN')")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String name) {
        return ResponseEntity.ok(userService.searchUsersByName(name));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User has been deleted");
        response.put("userId", userId.toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/by-role")
    @PreAuthorize("hasAnyRole('SELLER', 'BUYER', 'SYSTEMADMIN', 'CONTENTADMIN')")
    public ResponseEntity<Map<String, Integer>> getAdminIdByRole(@RequestParam String role) {
        Integer adminId = userService.getAdminIdByRole(role);
        Map<String, Integer> response = new HashMap<>();
        response.put("adminId", adminId);
        return ResponseEntity.ok(response);
    }
}
