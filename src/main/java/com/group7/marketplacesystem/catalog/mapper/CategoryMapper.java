package com.group7.marketplacesystem.catalog.mapper;

import com.group7.marketplacesystem.catalog.dto.request.CategoryRequest;
import com.group7.marketplacesystem.catalog.dto.response.CategoryResponse;
import com.group7.marketplacesystem.catalog.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request){
        if(request == null){
            return null;
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return  category;
    }

    public CategoryResponse toResponse(Category category){
        if ((category == null)){
            return null;
        }

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
