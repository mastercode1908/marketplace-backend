package com.group7.marketplacesystem.catalog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * DTO for {@link com.group7.marketplacesystem.catalog.entity.Product}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {

    @NotNull(message = "Category ID không được để trống")
    @Positive(message = "Category ID phải là số dương")
    private Integer categoryId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 1, max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    @Size(max = 5000, message = "Mô tả sản phẩm không được vượt quá 5000 ký tự")
    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá sản phẩm phải lớn hơn 0")
    @DecimalMax(value = "999999999999.99", message = "Giá sản phẩm quá lớn")
    private BigDecimal price;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    @Max(value = 999999999, message = "Số lượng tồn kho quá lớn")
    private Integer stockQuantity;

    private String productStatus;

    private String text;
}

// @FieldDefaults(level = AccessLevel.PRIVATE)
// chi can khi k muon go private