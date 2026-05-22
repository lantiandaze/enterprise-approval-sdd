package com.company.approval.system.controller;

import java.util.List;
import javax.validation.Valid;

import com.company.approval.common.exception.BusinessException;
import com.company.approval.common.exception.ErrorCode;
import com.company.approval.common.response.ApiResponse;
import com.company.approval.security.principal.CurrentUserProvider;
import com.company.approval.system.application.UserManagementApplicationService;
import com.company.approval.system.dto.RoleResponse;
import com.company.approval.system.dto.UserRequest;
import com.company.approval.system.dto.UserResponse;
import com.company.approval.system.dto.UserRoleRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserManagementController {

    private final UserManagementApplicationService userManagementApplicationService;
    private final CurrentUserProvider currentUserProvider;

    public UserManagementController(
            UserManagementApplicationService userManagementApplicationService,
            CurrentUserProvider currentUserProvider) {
        this.userManagementApplicationService = userManagementApplicationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/users")
    public ApiResponse<List<UserResponse>> listUsers() {
        return ApiResponse.success(userManagementApplicationService.listUsers());
    }

    @PostMapping("/users")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ApiResponse.success(userManagementApplicationService.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ApiResponse.success(userManagementApplicationService.updateUser(id, request));
    }

    @PatchMapping("/users/{id}/status")
    public ApiResponse<UserResponse> setUserStatus(@PathVariable Long id, @RequestParam String status) {
        if (currentUserProvider.getCurrentUser().getUserId().equals(id) && "disabled".equals(status)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Cannot disable current user");
        }
        return ApiResponse.success(userManagementApplicationService.setUserStatus(id, status));
    }

    @PutMapping("/users/{id}/roles")
    public ApiResponse<UserResponse> assignRoles(@PathVariable Long id, @Valid @RequestBody UserRoleRequest request) {
        return ApiResponse.success(userManagementApplicationService.assignRoles(
                id,
                request.getRoleIds(),
                currentUserProvider.getCurrentUser().getUserId()));
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        if (currentUserProvider.getCurrentUser().getUserId().equals(id)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Cannot delete current user");
        }
        userManagementApplicationService.deleteUser(id, currentUserProvider.getCurrentUser().getUserId());
        return ApiResponse.success(null);
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> listRoles() {
        return ApiResponse.success(userManagementApplicationService.listRoles());
    }
}
