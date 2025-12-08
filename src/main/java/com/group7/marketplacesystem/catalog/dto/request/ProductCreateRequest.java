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

    @NotNull(message = "Danh mục sản phẩm không được để trống")
    @Positive(message = "Danh mục sản phẩm không hợp lệ")
    private Integer categoryId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 1, max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    @NotBlank(message = "Mô tả sản phẩm không được để trống")
    @Size(min = 10, max = 2000, message = "Mô tả sản phẩm phải từ 10-2000 ký tự")
    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "1", message = "Giá sản phẩm phải lớn hơn 1000")
    @DecimalMax(value = "1000000000", message = "Giá sản phẩm không được vượt quá 1 tỷ VNĐ")
    private BigDecimal price;

    @NotNull(message = "Trọng lượng sản phẩm không được để trống")
    @Min(value = 1, message = "Trọng lượng sản phẩm phải lớn hơn 0 gram")
    @Max(value = 100000, message = "Trọng lượng sản phẩm không được vượt quá 100.000 gram")
    private Integer weight; // gram

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 1, message = "Số lượng tồn kho không được âm")
    @Max(value = 100000, message = "Số lượng tồn kho không được vượt quá 100.000")
    private Integer stockQuantity;

    private String productStatus = "Pending";

    private List<ProductMediaRequest> media;

}
