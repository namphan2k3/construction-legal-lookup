package com.constructionlegallookup.construction_legal_lookup_app.controllers;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.user.ChangePasswordRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.user.UserUpdateProfileRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.ApiResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.user.UserResponse;
import com.constructionlegallookup.construction_legal_lookup_app.services.UserService;
import com.constructionlegallookup.construction_legal_lookup_app.utils.LocalizationUtils;
import com.constructionlegallookup.construction_legal_lookup_app.utils.MessageKeys;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final LocalizationUtils localizationUtils;

    public UserController(UserService userService, LocalizationUtils localizationUtils) {
        this.userService = userService;
        this.localizationUtils = localizationUtils;
    }

    @GetMapping({"/me", "/profile"})
    public ApiResponse<UserResponse> getMyInfo() {
        UserResponse userResponse = userService.getProfile();
        return ApiResponse.<UserResponse>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.USER_PROFILE_SUCCESS))
                .data(userResponse)
                .build();
    }

    @PutMapping("/profile")
    public ApiResponse<UserResponse> updateProfile(@Valid @RequestBody UserUpdateProfileRequest request) {
        UserResponse userResponse = userService.updateProfile(request);
        return ApiResponse.<UserResponse>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.USER_UPDATE_SUCCESS))
                .data(userResponse)
                .build();
    }

    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.<Void>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.USER_PASSWORD_CHANGED))
                .build();
    }
}
