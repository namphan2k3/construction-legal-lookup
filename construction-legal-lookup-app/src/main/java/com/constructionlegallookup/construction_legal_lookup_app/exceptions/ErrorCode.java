package com.constructionlegallookup.construction_legal_lookup_app.exceptions;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_ERROR("uncategorized_error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT("invalid_input", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("bad_request", HttpStatus.BAD_REQUEST),
    NOT_FOUND("not_found", HttpStatus.NOT_FOUND),
    CONFLICT("conflict", HttpStatus.CONFLICT),
    USER_NOT_EXISTED("user.not_existed", HttpStatus.NOT_FOUND),
    USER_EXISTED("user.existed", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXISTED("role_not_existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED("unauthenticated", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("forbidden", HttpStatus.FORBIDDEN),
    INVALID_TOKEN("invalid_token", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED("account_disabled", HttpStatus.FORBIDDEN),
    EMAIL_ALREADY_EXISTS("email_already_exists", HttpStatus.CONFLICT),
    EMAIL_EXISTED("email.existed", HttpStatus.CONFLICT),
    DOCUMENT_NOT_EXISTED("document.not_existed", HttpStatus.NOT_FOUND),
    DOCUMENT_NOT_FOUND("document.not_found", HttpStatus.NOT_FOUND),
    BOOKMARK_NOT_FOUND("bookmark.not_found", HttpStatus.NOT_FOUND),
    BOOKMARK_ALREADY_EXISTS("bookmark.already_exists", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND("category.not_found", HttpStatus.NOT_FOUND),
    TAG_NOT_FOUND("tag.not_found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("user.not_found", HttpStatus.NOT_FOUND),
    AI_RATE_LIMIT_EXCEEDED("ai.rate_limit_exceeded", HttpStatus.TOO_MANY_REQUESTS),
    UPLOAD_FILE_FAILED("upload.file_failed", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus httpStatus;
}
