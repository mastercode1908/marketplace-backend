package com.group7.marketplacesystem.catalog.controller;

import com.group7.marketplacesystem.catalog.dto.response.CategoryResponse;
import com.group7.marketplacesystem.catalog.service.CategoryService;
import com.group7.marketplacesystem.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategory(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 100, page = 0) Pageable pageable
    ) {
        List<CategoryResponse> responses = categoryService.getAllCategory(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
