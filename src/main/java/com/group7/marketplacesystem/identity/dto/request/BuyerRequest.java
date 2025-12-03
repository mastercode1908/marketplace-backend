package com.group7.marketplacesystem.identity.dto.request;

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
public class BuyerRequest {

    private String address;

    private LocalDate dateOfBirth;

//    private Instant deletedAt;
}
