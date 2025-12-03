package com.group7.marketplacesystem.identity.controller;

import com.group7.marketplacesystem.common.constants.MessageConstants;
import com.group7.marketplacesystem.common.response.ApiResponse;
import com.group7.marketplacesystem.identity.dto.request.UserRegisterRequest;
import com.group7.marketplacesystem.identity.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegisterController {
    private final RegisterService registerService;

    @PostMapping("/register/user")
    public ResponseEntity<ApiResponse<String>> registerBuyer(@Valid @RequestBody UserRegisterRequest request) {

        registerService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MessageConstants.REGISTER_SUCCESS, null));

    }
}
