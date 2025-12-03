package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuyerResponse {

    private Integer buyerId;

    private String address;

    private LocalDate dateOfBirth;

    private Instant deletedAt;

    private String username;

    private String email;

    private String fullName;

    private String phone;

    private Boolean gender;

    private String avatarUrl;

    private String role;

    private String userStatus;

    private Instant createdAt;
}
