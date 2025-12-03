package com.group7.marketplacesystem.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Xử lý lỗi custom của hệ thống, chỉ bắt lỗi phát sinh trong controller hoặc service.
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        ApiErrorResponse response = new ApiErrorResponse(
                ex.getCode(),
                ex.getErrorCode().getMessage(),
                ex.getStatusCode().name(),
                System.currentTimeMillis(),
                request.getRequestURI(),
                ex.getErrors()
        );
        log.error("API Exception: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    // Lỗi khi validate input (ví dụ @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = ((FieldError) err).getField();
            String message = err.getDefaultMessage();
            errors.put(field, message);
        });

        ApiErrorResponse response = new ApiErrorResponse(
                ErrorCode.VALIDATION_FAILED.getCode(),
                ErrorCode.VALIDATION_FAILED.getMessage(),
                ErrorCode.VALIDATION_FAILED.getStatusCode().name(),
                System.currentTimeMillis(),
                request.getRequestURI(),
                errors
        );

        log.warn("Validation error: {}", errors);
        return ResponseEntity.badRequest().body(response);
    }

    // Khi người dùng có token nhưng không đủ quyền (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ApiErrorResponse response = new ApiErrorResponse(
                ErrorCode.UNAUTHORIZED.getCode(),
                ErrorCode.UNAUTHORIZED.getMessage(),
                ErrorCode.UNAUTHORIZED.getStatusCode().name(),
                System.currentTimeMillis(),
                request.getRequestURI(),
                null
        );
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // Khi chưa đăng nhập hoặc token sai (401)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        ApiErrorResponse response = new ApiErrorResponse(
                ErrorCode.UNAUTHENTICATED.getCode(),
                ErrorCode.UNAUTHENTICATED.getMessage(),
                ErrorCode.UNAUTHENTICATED.getStatusCode().name(),
                System.currentTimeMillis(),
                request.getRequestURI(),
                null
        );
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Khi lỗi constraint trong DB (unique key, foreign key, v.v.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        ApiErrorResponse response = new ApiErrorResponse(
                ErrorCode.DATABASE_CONSTRAINT_VIOLATION.getCode(),
                ErrorCode.DATABASE_CONSTRAINT_VIOLATION.getMessage() + ": " + ex.getMostSpecificCause().getMessage(),
                ErrorCode.DATABASE_CONSTRAINT_VIOLATION.getStatusCode().name(),
                System.currentTimeMillis(),
                request.getRequestURI(),
                null
        );
        log.error("Data integrity error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {

        ApiErrorResponse response = new ApiErrorResponse(
                ErrorCode.BAD_REQUEST.getCode(),
                ex.getMessage(),
                ErrorCode.BAD_REQUEST.getStatusCode().name(),
                System.currentTimeMillis(),
                request.getRequestURI(),
                null
        );

        log.warn("IllegalStateException: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    // ⚡ Bắt tất cả lỗi còn lại
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        ApiErrorResponse response = new ApiErrorResponse(
                ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(),
                ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage(),
                ErrorCode.UNCATEGORIZED_EXCEPTION.getStatusCode().name(),
                System.currentTimeMillis(),
                request.getRequestURI(),
                null
        );
        log.error("Unhandled exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
