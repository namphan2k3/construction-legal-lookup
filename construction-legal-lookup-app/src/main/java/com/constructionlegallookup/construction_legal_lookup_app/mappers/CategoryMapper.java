package com.constructionlegallookup.construction_legal_lookup_app.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.CategoryAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.CategoryResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toCategoryResponse(Category category);
    Category toCategory(CategoryAdminRequest request);
    void updateCategory(@MappingTarget Category category, CategoryAdminRequest request);
}
