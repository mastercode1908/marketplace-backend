package com.group7.marketplacesystem.common.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ApiException extends RuntimeException{
    private final ErrorCode errorCode;
    private Map<String, String> errors;


    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // Constructor nhận map field-level lỗi
    public ApiException(ErrorCode errorCode, Map<String, String> errors) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getCode() {
        return errorCode.getCode();
    }

    public HttpStatus getStatusCode() {
        return errorCode.getStatusCode();
    }
}
