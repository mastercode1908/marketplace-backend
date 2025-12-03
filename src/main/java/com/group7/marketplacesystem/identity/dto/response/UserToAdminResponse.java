package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserToAdminResponse {
    private Integer id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private String avatarUrl;
    private Boolean emailVerified;
    private String role;
    private Boolean gender;
    private String userStatus;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}
