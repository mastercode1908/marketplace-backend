package com.group7.marketplacesystem.catalog.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 1000)
    private String description;
}
