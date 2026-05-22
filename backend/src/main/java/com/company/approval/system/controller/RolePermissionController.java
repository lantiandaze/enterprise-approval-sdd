package com.company.approval.system.controller;

import java.util.List;
import javax.validation.Valid;

import com.company.approval.common.response.ApiResponse;
import com.company.approval.security.principal.CurrentUserProvider;
import com.company.approval.system.application.RolePermissionApplicationService;
import com.company.approval.system.dto.PermissionResponse;
import com.company.approval.system.dto.RolePermissionRequest;
import com.company.approval.system.dto.RolePermissionResponse;
import com.company.approval.system.dto.RoleResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rbac")
public class RolePermissionController {

    private final RolePermissionApplicationService rolePermissionApplicationService;
    private final CurrentUserProvider currentUserProvider;

    public RolePermissionController(
            RolePermissionApplicationService rolePermissionApplicationService,
            CurrentUserProvider currentUserProvider) {
        this.rolePermissionApplicationService = rolePermissionApplicationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> listRoles() {
        return ApiResponse.success(rolePermissionApplicationService.listRoles());
    }

    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponse>> listPermissions() {
        return ApiResponse.success(rolePermissionApplicationService.listPermissions());
    }

    @GetMapping("/roles/{roleId}/permissions")
    public ApiResponse<RolePermissionResponse> getRolePermissions(@PathVariable Long roleId) {
        return ApiResponse.success(rolePermissionApplicationService.getRolePermissions(roleId));
    }

    @PutMapping("/roles/{roleId}/permissions")
    public ApiResponse<RolePermissionResponse> saveRolePermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody RolePermissionRequest request) {
        return ApiResponse.success(rolePermissionApplicationService.saveRolePermissions(
                roleId,
                request.getPermissionIds(),
                currentUserProvider.getCurrentUser().getUserId()));
    }
}
