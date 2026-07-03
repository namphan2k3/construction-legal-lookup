package com.constructionlegallookup.construction_legal_lookup_app.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.ApiResponse;
import com.constructionlegallookup.construction_legal_lookup_app.utils.LocalizationUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GlobalException {
    LocalizationUtils localizationUtils;

    @ExceptionHandler(exception = AppException.class)
    public ResponseEntity<ApiResponse<?>> handlingAppException(AppException e) {
        String message = localizationUtils.getLocalizedMessage(e.getErrorCode().getMessage());
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(false)
                .message(message)
                .build();
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(false)
                .message("Invalid input")
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception e) {
        String message = localizationUtils.getLocalizedMessage(ErrorCode.UNCATEGORIZED_ERROR.getMessage());
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(false)
                .message(message + ": " + e.getMessage())
                .build();
        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_ERROR.getHttpStatus()).body(apiResponse);
    }
}
