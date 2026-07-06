package com.constructionlegallookup.construction_legal_lookup_app.controllers;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.UpdateRoleRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.CreateUserRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.UpdateUserRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.AdminUserDto;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.ApiResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.PaginationInfo;
import com.constructionlegallookup.construction_legal_lookup_app.services.AdminUserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Admin - Users", description = "Admin user management")
public class AdminUserController {

    AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminUserDto>>> getUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminUserDto> usersPage = adminUserService.getUsers(q, enabled, role, pageable);
        PaginationInfo pagination = PaginationInfo.builder()
                .page(page)
                .size(size)
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .first(usersPage.isFirst())
                .last(usersPage.isLast())
                .build();
        ApiResponse<Page<AdminUserDto>> response = ApiResponse.<Page<AdminUserDto>>builder()
                .success(true)
                .data(usersPage)
                .paginationInfo(pagination)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> disableUser(@PathVariable Long id) {
        adminUserService.disableUser(id);
        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .data(Map.of("message", "Đã khóa tài khoản", "enabled", false))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> enableUser(@PathVariable Long id) {
        adminUserService.enableUser(id);
        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .data(Map.of("message", "Đã mở khóa tài khoản", "enabled", true))
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        adminUserService.updateUserRole(id, request);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
        AdminUserDto created = adminUserService.createUser(request);
        ApiResponse<AdminUserDto> response = ApiResponse.<AdminUserDto>builder()
                .success(true)
                .data(created)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDto>> updateUser(
            @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        AdminUserDto updated = adminUserService.updateUser(id, request);
        ApiResponse<AdminUserDto> response = ApiResponse.<AdminUserDto>builder()
                .success(true)
                .data(updated)
                .build();
        return ResponseEntity.ok(response);
    }
}
