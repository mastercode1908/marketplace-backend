package com.group7.marketplacesystem.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiPageResponse<T> {
    private boolean success;
    private String message;
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static <T> ApiPageResponse<T> fromPage(Page<T> pageData) {
        return ApiPageResponse.<T>builder()
                .success(true)
                .message("Page fetched successfully")
                .content(pageData.getContent())
                .page(pageData.getNumber())
                .size(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .last(pageData.isLast())
                .build();
    }
}
