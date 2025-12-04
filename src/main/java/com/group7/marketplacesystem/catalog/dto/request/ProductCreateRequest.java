package com.group7.marketplacesystem.catalog.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductCreateRequest {

    @NotNull
    private Integer categoryId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 1, max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    @Size(min = 10, max = 2000, message = "Mô tả sản phẩm không được vượt quá 2000 ký tự")
    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0", message = "Giá sản phẩm phải lớn hơn hoặc bằng 0")
    @DecimalMax(value = "1000000000", message = "Giá sản phẩm quá lớn")
    private BigDecimal price;

    @NotNull
    @DecimalMin(value = "0", message = "Giá sản phẩm phải lớn hơn hoặc bằng 0")
    @DecimalMax(value = "100000", message = "Giá sản phẩm quá lớn")
    private Integer weight; // gram

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    @Max(value = 100000, message = "Số lượng tồn kho quá lớn")
    private Integer stockQuantity;

    private String productStatus = "Pending";

    private List<ProductMediaRequest> media;

}
