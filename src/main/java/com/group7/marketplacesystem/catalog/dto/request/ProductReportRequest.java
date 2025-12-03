package com.group7.marketplacesystem.catalog.dto.request;

import com.group7.marketplacesystem.catalog.entity.Productreport;
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
public class ProductReportRequest {
    private Integer productId;

    @NotBlank(message = "Reason cannot be empty")
    @Size(min = 4, max = 255, message = "Reason must be between 4 and 255 characters")
    private String reason;

    private String status;

    private Integer reportId;

    private Productreport report;

    private Integer orderDetailId;

    private java.util.List<com.group7.marketplacesystem.catalog.dto.common.ReportMediaDTO> media;
}
