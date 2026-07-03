package com.constructionlegallookup.construction_legal_lookup_app.services.impl;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.auth.AuthenticationRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.auth.LogoutRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.auth.RegisterRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.auth.AuthResult;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.auth.TokenInfo;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.user.UserResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.InvalidatedToken;
import com.constructionlegallookup.construction_legal_lookup_app.entities.User;
import com.constructionlegallookup.construction_legal_lookup_app.enums.UserRole;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.AppException;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.ErrorCode;
import com.constructionlegallookup.construction_legal_lookup_app.mappers.UserMapper;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.InvalidatedTokenRepository;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.UserRepository;
import com.constructionlegallookup.construction_legal_lookup_app.services.AuthService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RedisTemplate<String, String> redisTemplate;
    InvalidatedTokenRepository invalidatedTokenRepository;
    UserMapper userMapper;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_REFRESH_TOKENS_PREFIX = "user_refresh_tokens:";

    @NonFinal
    @Value("${jwt.signer-key}")
    String signerKey;

    @NonFinal
    @Value("${jwt.valid-duration}")
    Long validDuration;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    Long refreshableDuration;

    @Override
    @Transactional
    public AuthResult register(RegisterRequest request) throws JOSEException {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.USER)
                .enabled(true)
                .build();

        user = userRepository.save(user);

        return buildAuthResult(user);
    }

    @Override
    public AuthResult login(AuthenticationRequest request) throws JOSEException {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!user.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return buildAuthResult(user);
    }

    @Override
    public AuthResult refreshToken(String refreshToken) throws ParseException, JOSEException {
        SignedJWT signedJwt = verifyToken(refreshToken);
        String sub = signedJwt.getJWTClaimsSet().getSubject();
        String jti = signedJwt.getJWTClaimsSet().getJWTID();

        String key = REFRESH_TOKEN_PREFIX + jti;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = userRepository.findById(Long.valueOf(sub))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        revokeRefreshToken(jti, sub);

        return buildAuthResult(user);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request, String refreshTokenFromCookie) throws ParseException, JOSEException {
        SignedJWT signedJwt = SignedJWT.parse(request.getAccessToken());
        String jti = signedJwt.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJwt.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jti)
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);

        if (refreshTokenFromCookie != null && !refreshTokenFromCookie.isBlank()) {
            try {
                SignedJWT refreshJwt = SignedJWT.parse(refreshTokenFromCookie);
                String refreshJti = refreshJwt.getJWTClaimsSet().getJWTID();
                String refreshSub = refreshJwt.getJWTClaimsSet().getSubject();
                revokeRefreshToken(refreshJti, refreshSub);
            } catch (Exception ignored) {
            }
        }
    }

    private void revokeRefreshToken(String jti, String userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + jti);
        redisTemplate.opsForSet().remove(USER_REFRESH_TOKENS_PREFIX + userId, jti);
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier jwsVerifier = new MACVerifier(signerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean verify = signedJWT.verify(jwsVerifier);

        if (!verify || expiryTime.before(new Date())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    private String generateToken(User user, boolean isRefreshToken) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        long expirationTime = isRefreshToken ? refreshableDuration : validDuration;

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .issuer("constructionlegallookup")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(expirationTime, ChronoUnit.SECONDS)))
                .jwtID(UUID.randomUUID().toString());

        if (!isRefreshToken) {
            claimsBuilder.claim("email", user.getEmail());
            claimsBuilder.claim("role", user.getRole().name());
        }

        JWTClaimsSet claimsSet = claimsBuilder.build();
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        jwsObject.sign(new MACSigner(signerKey));

        return jwsObject.serialize();
    }

    private AuthResult buildAuthResult(User user) throws JOSEException {
        String accessToken = generateToken(user, false);
        String refreshToken = generateToken(user, true);

        // Store refresh token in Redis
        storeRefreshToken(user.getId().toString(), refreshToken);

        UserResponse userResponse = userMapper.toUserResponse(user);
        Date expiresAt = Date.from(Instant.now().plusSeconds(validDuration));
        Date refreshExpiresAt = Date.from(Instant.now().plusSeconds(refreshableDuration));

        TokenInfo tokenInfo = TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .refreshExpiresAt(refreshExpiresAt)
                .refreshMaxAge(refreshableDuration)
                .build();

        return AuthResult.builder()
                .user(userResponse)
                .tokenInfo(tokenInfo)
                .build();
    }

    private void storeRefreshToken(String userId, String refreshToken) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(refreshToken);
            String jti = signedJwt.getJWTClaimsSet().getJWTID();
            String key = REFRESH_TOKEN_PREFIX + jti;

            redisTemplate.opsForValue().set(key, userId, refreshableDuration, TimeUnit.SECONDS);
            redisTemplate.opsForSet().add(USER_REFRESH_TOKENS_PREFIX + userId, jti);
            redisTemplate.expire(USER_REFRESH_TOKENS_PREFIX + userId, refreshableDuration, TimeUnit.SECONDS);
        } catch (ParseException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    @Override
    public void revokeAllUserSessions(Long userId) {
        String userIdStr = userId.toString();
        String userRefreshTokensKey = USER_REFRESH_TOKENS_PREFIX + userIdStr;

        // Get all refresh token jtis for this user
        var jtis = redisTemplate.opsForSet().members(userRefreshTokensKey);
        if (jtis != null) {
            for (String jti : jtis) {
                redisTemplate.delete(REFRESH_TOKEN_PREFIX + jti);
            }
        }

        // Delete the user's refresh tokens set
        redisTemplate.delete(userRefreshTokensKey);
    }
}
