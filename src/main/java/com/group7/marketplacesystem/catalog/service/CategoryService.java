package com.group7.marketplacesystem.catalog.service;

import com.group7.marketplacesystem.catalog.dto.request.CategoryRequest;
import com.group7.marketplacesystem.catalog.dto.response.CategoryResponse;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategory(String keyword, Pageable pageable);
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Integer id, CategoryRequest request);
    void deleteCategory(Integer id);

}
