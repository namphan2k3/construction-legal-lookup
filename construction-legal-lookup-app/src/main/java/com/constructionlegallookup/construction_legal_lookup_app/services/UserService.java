package com.constructionlegallookup.construction_legal_lookup_app.services;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.user.ChangePasswordRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.user.UserUpdateProfileRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.user.UserResponse;

public interface UserService {
    UserResponse getProfile();
    UserResponse updateProfile(UserUpdateProfileRequest request);
    void changePassword(ChangePasswordRequest request);
}
