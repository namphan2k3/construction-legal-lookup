package com.constructionlegallookup.construction_legal_lookup_app.services;

import java.util.List;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.CategoryAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.CategoryResponse;

public interface AdminCategoryService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse createCategory(CategoryAdminRequest request);
    CategoryResponse updateCategory(Long id, CategoryAdminRequest request);
    void deleteCategory(Long id);
}
