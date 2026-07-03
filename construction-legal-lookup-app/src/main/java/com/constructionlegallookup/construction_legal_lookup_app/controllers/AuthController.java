package com.constructionlegallookup.construction_legal_lookup_app.controllers;

import java.text.ParseException;
import java.time.Duration;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import com.nimbusds.jose.JOSEException;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.auth.AuthenticationRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.auth.LogoutRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.auth.RegisterRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.auth.AuthResult;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.auth.AuthenticationResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.ApiResponse;
import com.constructionlegallookup.construction_legal_lookup_app.services.AuthService;
import com.constructionlegallookup.construction_legal_lookup_app.utils.LocalizationUtils;
import com.constructionlegallookup.construction_legal_lookup_app.utils.MessageKeys;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final LocalizationUtils localizationUtils;

    public AuthController(AuthService authService, LocalizationUtils localizationUtils) {
        this.authService = authService;
        this.localizationUtils = localizationUtils;
    }

    @PostMapping("/register")
    public ApiResponse<AuthenticationResponse> register(
            @RequestBody @Valid RegisterRequest request, HttpServletResponse response) throws JOSEException {
        AuthResult authResult = authService.register(request);

        setRefreshTokenCookie(response, authResult);

        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .user(authResult.getUser())
                .accessToken(authResult.getTokenInfo().getAccessToken())
                .expiresAt(authResult.getTokenInfo().getExpiresAt())
                .build();

        return ApiResponse.<AuthenticationResponse>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.USER_REGISTER_SUCCESS))
                .data(authResponse)
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(
            @RequestBody @Valid AuthenticationRequest request, HttpServletResponse response) throws JOSEException {
        AuthResult authResult = authService.login(request);

        setRefreshTokenCookie(response, authResult);

        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .user(authResult.getUser())
                .accessToken(authResult.getTokenInfo().getAccessToken())
                .expiresAt(authResult.getTokenInfo().getExpiresAt())
                .build();

        return ApiResponse.<AuthenticationResponse>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.USER_LOGIN_SUCCESS))
                .data(authResponse)
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(
            @CookieValue("refreshToken") String refreshToken, HttpServletResponse response)
            throws ParseException, JOSEException {
        AuthResult authResult = authService.refreshToken(refreshToken);

        setRefreshTokenCookie(response, authResult);

        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .user(authResult.getUser())
                .accessToken(authResult.getTokenInfo().getAccessToken())
                .expiresAt(authResult.getTokenInfo().getExpiresAt())
                .build();

        return ApiResponse.<AuthenticationResponse>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.USER_REFRESH_SUCCESS))
                .data(authResponse)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestBody @Valid LogoutRequest request,
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) throws ParseException, JOSEException {
        authService.logout(request, refreshToken);

        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .sameSite("None")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        return ApiResponse.<Void>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.USER_LOGOUT_SUCCESS))
                .build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, AuthResult authResult) {
        ResponseCookie responseCookie = ResponseCookie
                .from("refreshToken", authResult.getTokenInfo().getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .sameSite("None")
                .maxAge(Duration.ofSeconds(authResult.getTokenInfo().getRefreshMaxAge()))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }
}
