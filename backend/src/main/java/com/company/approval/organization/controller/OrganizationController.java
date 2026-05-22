package com.company.approval.organization.controller;

import java.util.List;

import javax.validation.Valid;

import com.company.approval.common.response.ApiResponse;
import com.company.approval.organization.application.OrganizationApplicationService;
import com.company.approval.organization.dto.DepartmentRequest;
import com.company.approval.organization.dto.DepartmentResponse;
import com.company.approval.organization.dto.PositionRequest;
import com.company.approval.organization.dto.PositionResponse;
import com.company.approval.security.principal.CurrentUserProvider;
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
@RequestMapping("/api/organization")
public class OrganizationController {

    private final OrganizationApplicationService organizationApplicationService;
    private final CurrentUserProvider currentUserProvider;

    public OrganizationController(
            OrganizationApplicationService organizationApplicationService,
            CurrentUserProvider currentUserProvider) {
        this.organizationApplicationService = organizationApplicationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/departments/tree")
    public ApiResponse<List<DepartmentResponse>> getDepartmentTree() {
        return ApiResponse.success(organizationApplicationService.getDepartmentTree());
    }

    @PostMapping("/departments")
    public ApiResponse<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.success(organizationApplicationService.createDepartment(request));
    }

    @PutMapping("/departments/{id}")
    public ApiResponse<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.success(organizationApplicationService.updateDepartment(id, request));
    }

    @PatchMapping("/departments/{id}/enabled")
    public ApiResponse<DepartmentResponse> setDepartmentEnabled(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        return ApiResponse.success(organizationApplicationService.setDepartmentEnabled(id, enabled));
    }

    @DeleteMapping("/departments/{id}")
    public ApiResponse<Void> deleteDepartment(@PathVariable Long id) {
        organizationApplicationService.deleteDepartment(id, currentUserProvider.getCurrentUser().getUserId());
        return ApiResponse.success(null);
    }

    @GetMapping("/positions")
    public ApiResponse<List<PositionResponse>> listPositions(@RequestParam(required = false) Long departmentId) {
        return ApiResponse.success(organizationApplicationService.listPositions(departmentId));
    }

    @PostMapping("/positions")
    public ApiResponse<PositionResponse> createPosition(@Valid @RequestBody PositionRequest request) {
        return ApiResponse.success(organizationApplicationService.createPosition(request));
    }

    @PutMapping("/positions/{id}")
    public ApiResponse<PositionResponse> updatePosition(
            @PathVariable Long id,
            @Valid @RequestBody PositionRequest request) {
        return ApiResponse.success(organizationApplicationService.updatePosition(id, request));
    }

    @PatchMapping("/positions/{id}/enabled")
    public ApiResponse<PositionResponse> setPositionEnabled(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        return ApiResponse.success(organizationApplicationService.setPositionEnabled(id, enabled));
    }

    @DeleteMapping("/positions/{id}")
    public ApiResponse<Void> deletePosition(@PathVariable Long id) {
        organizationApplicationService.deletePosition(id, currentUserProvider.getCurrentUser().getUserId());
        return ApiResponse.success(null);
    }
}
