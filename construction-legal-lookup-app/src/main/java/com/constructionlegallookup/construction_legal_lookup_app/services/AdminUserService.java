package com.constructionlegallookup.construction_legal_lookup_app.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.CreateUserRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.UpdateRoleRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.AdminUserDto;

public interface AdminUserService {
    Page<AdminUserDto> getUsers(String q, Boolean enabled, String role, Pageable pageable);
    void disableUser(Long id);
    void enableUser(Long id);
    void updateUserRole(Long id, UpdateRoleRequest request);
    AdminUserDto createUser(CreateUserRequest request);
}
