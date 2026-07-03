package com.constructionlegallookup.construction_legal_lookup_app.services.impl;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.user.ChangePasswordRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.user.UserUpdateProfileRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.user.UserResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.User;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.AppException;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.ErrorCode;
import com.constructionlegallookup.construction_legal_lookup_app.mappers.UserMapper;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.UserRepository;
import com.constructionlegallookup.construction_legal_lookup_app.services.AuthService;
import com.constructionlegallookup.construction_legal_lookup_app.services.UserService;
import com.constructionlegallookup.construction_legal_lookup_app.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    UserMapper userMapper;
    SecurityUtils securityUtils;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuthService authService;

    @Override
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public UserResponse getProfile() {
        User user = securityUtils.getCurrentUser();
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public UserResponse updateProfile(UserUpdateProfileRequest request) {
        User user = securityUtils.getCurrentUser();
        user.setFullName(request.getFullName());
        user = userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public void changePassword(ChangePasswordRequest request) {
        User user = securityUtils.getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Mật khẩu hiện tại không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all user's refresh tokens (if implemented in authService)
        authService.revokeAllUserSessions(user.getId());
    }
}
