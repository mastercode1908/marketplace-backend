package com.group7.marketplacesystem.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    private int code;
    private String message;
    private String status;
    private long timestamp;
    private String path;

    // Thêm field riêng để chứa lỗi từng field
    private Map<String, String> errors;
}
