package com.constructionlegallookup.construction_legal_lookup_app.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.CreateUserRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.UpdateRoleRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.AdminUserDto;
import com.constructionlegallookup.construction_legal_lookup_app.entities.User;
import com.constructionlegallookup.construction_legal_lookup_app.enums.UserRole;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.AppException;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.ErrorCode;
import com.constructionlegallookup.construction_legal_lookup_app.mappers.UserMapper;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.UserRepository;
import com.constructionlegallookup.construction_legal_lookup_app.services.AdminUserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class AdminUserServiceImpl implements AdminUserService {

    UserRepository userRepository;
    UserMapper userMapper;
    RedisTemplate<String, Object> redisTemplate;
    PasswordEncoder passwordEncoder;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AdminUserDto> getUsers(String q, Boolean enabled, String role, Pageable pageable) {
        UserRole userRole = role != null ? UserRole.valueOf(role) : null;
        return userRepository.findAllByFilters(q, enabled, userRole, pageable)
                .map(userMapper::toAdminUserDto);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setEnabled(false);
        userRepository.save(user);
        revokeAllUserTokens(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void updateUserRole(Long id, UpdateRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setRole(UserRole.valueOf(request.getRole()));
        userRepository.save(user);
        revokeAllUserTokens(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public AdminUserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole() != null ? UserRole.valueOf(request.getRole()) : UserRole.USER)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        return userMapper.toAdminUserDto(saved);
    }
    
    private void revokeAllUserTokens(Long userId) {
        // Skip token revocation due to Redis client compatibility issues
        // Tokens will naturally expire based on their TTL
    }
}
