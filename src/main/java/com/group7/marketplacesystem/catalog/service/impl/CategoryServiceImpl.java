package com.group7.marketplacesystem.catalog.service.impl;

import com.group7.marketplacesystem.catalog.dto.request.CategoryRequest;
import com.group7.marketplacesystem.catalog.dto.response.CategoryResponse;
import com.group7.marketplacesystem.catalog.entity.Category;
import com.group7.marketplacesystem.catalog.mapper.CategoryMapper;
import com.group7.marketplacesystem.catalog.repository.CategoryRepository;
import com.group7.marketplacesystem.catalog.service.CategoryService;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAllCategory(String keyword, org.springframework.data.domain.Pageable pageable) {
        return categoryRepository.findByNameContainingIgnoreCase(keyword, pageable)
                .map(categoryMapper::toResponse)
                .getContent();
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(validate(request.getName(), ErrorCode.INVALID_CATEGORY_NAME));
        category.setDescription(validate(request.getDescription(), ErrorCode.INVALID_DESCRIPTION));

        Category categorySaved = categoryRepository.save(category);

        return categoryMapper.toResponse(categorySaved);
    }

    @Override
    public CategoryResponse updateCategory(Integer id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND));
        if(category.getDeletedAt()!= null){
            throw new ApiException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        category.setName(validate(request.getName(), ErrorCode.INVALID_CATEGORY_NAME));
        category.setDescription(validate(request.getDescription(), ErrorCode.INVALID_DESCRIPTION));

        categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Override
    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND));

        if (category.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        category.setDeletedAt(Instant.now());
        categoryRepository.save(category);
    }

    public String validate(String name, ErrorCode error) {
        if (name == null) {
            throw new ApiException(error);
        }
        // Kiểm tra space đầu
        if (name.startsWith(" ")) {
            throw new ApiException(error);
        }
        String cleaned = name.trim();
        if (cleaned.isEmpty()) {
            throw new ApiException(error);
        }
        if (cleaned.matches(".*\\s{2,}.*")) {
            throw new ApiException(error); // quá 1 space liên tiếp
        }
        if (!cleaned.matches("^[\\p{L}\\p{N}\\s.,!?:&()/\\-\"']+$")) {
            throw new ApiException(error);
        }
        return cleaned;
    }
}
