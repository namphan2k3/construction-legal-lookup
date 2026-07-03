package com.constructionlegallookup.construction_legal_lookup_app.services;

import java.text.ParseException;

import com.nimbusds.jose.JOSEException;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.auth.AuthenticationRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.auth.LogoutRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.auth.RegisterRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.auth.AuthResult;

public interface AuthService {
    AuthResult register(RegisterRequest request) throws JOSEException;
    AuthResult login(AuthenticationRequest request) throws JOSEException;
    AuthResult refreshToken(String refreshToken) throws ParseException, JOSEException;
    void logout(LogoutRequest request, String refreshTokenFromCookie) throws ParseException, JOSEException;
    void revokeAllUserSessions(Long userId);
}
