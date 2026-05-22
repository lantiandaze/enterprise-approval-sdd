package com.company.approval.auth.controller;

import com.company.approval.auth.application.AuthApplicationService;
import com.company.approval.auth.dto.CurrentUserResponse;
import com.company.approval.auth.dto.LoginRequest;
import com.company.approval.auth.dto.LoginResponse;
import com.company.approval.common.response.ApiResponse;
import com.company.approval.security.principal.CurrentUserProvider;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;
    private final CurrentUserProvider currentUserProvider;

    public AuthController(AuthApplicationService authApplicationService, CurrentUserProvider currentUserProvider) {
        this.authApplicationService = authApplicationService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authApplicationService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me() {
        return ApiResponse.success(CurrentUserResponse.from(currentUserProvider.getCurrentUser()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success(null);
    }
}

