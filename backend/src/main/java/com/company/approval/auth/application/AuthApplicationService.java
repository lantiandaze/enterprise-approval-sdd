package com.company.approval.auth.application;

import com.company.approval.auth.dto.CurrentUserResponse;
import com.company.approval.auth.dto.LoginRequest;
import com.company.approval.auth.dto.LoginResponse;
import com.company.approval.common.exception.BusinessException;
import com.company.approval.common.exception.ErrorCode;
import com.company.approval.security.jwt.JwtTokenService;
import com.company.approval.security.principal.UserPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {

    private final UserPrincipalService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthApplicationService(UserPrincipalService userService, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public LoginResponse login(LoginRequest request) {
        UserPrincipal principal = userService.loadByUsername(request.getUsername());
        if (principal == null || !passwordEncoder.matches(request.getPassword(), principal.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        String token = jwtTokenService.createToken(principal);
        return new LoginResponse(token, "Bearer", CurrentUserResponse.from(principal));
    }
}
