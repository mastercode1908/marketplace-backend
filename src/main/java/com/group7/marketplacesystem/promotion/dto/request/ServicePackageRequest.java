package com.group7.marketplacesystem.promotion.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServicePackageRequest {
    @NotBlank(message = "Package name cannot be blank")
    private String name;
    @NotBlank(message = "Package type cannot be blank")
    private String type;
    @NotBlank(message = "Description cannot be blank")
    private String description;
    @NotNull(message = "Price cannot be null")
    @Min(value = 0, message = "Price must be non-negative")
    private BigDecimal price;
    @NotNull(message = "Duration in days cannot be null")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;
    @Min(value = 0, message = "Usage limit must be non-negative")
    private Integer usageLimit;
}
