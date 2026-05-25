package com.company.approval.approval.controller;

import com.company.approval.approval.application.ApprovalManagementService;
import com.company.approval.approval.dto.ApprovalManagementQuery;
import com.company.approval.approval.dto.ApprovalManagementResponse;
import com.company.approval.approval.dto.DashboardSummaryResponse;
import com.company.approval.approval.dto.StatisticsResponse;
import com.company.approval.common.pagination.PagedResult;
import com.company.approval.common.response.ApiResponse;
import com.company.approval.security.principal.CurrentUserProvider;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/approval-management")
public class ApprovalManagementController {

    private final ApprovalManagementService service;
    private final CurrentUserProvider currentUserProvider;

    public ApprovalManagementController(ApprovalManagementService service, CurrentUserProvider currentUserProvider) {
        this.service = service;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ApiResponse<PagedResult<ApprovalManagementResponse>> list(@ModelAttribute ApprovalManagementQuery query) {
        return ApiResponse.success(service.list(query, currentUserProvider.getCurrentUser()));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@ModelAttribute ApprovalManagementQuery query) {
        byte[] bytes = service.exportCsv(query, currentUserProvider.getCurrentUser());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("approval-records.csv").build().toString())
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardSummaryResponse> dashboard() {
        return ApiResponse.success(service.dashboard(currentUserProvider.getCurrentUser()));
    }

    @GetMapping("/statistics")
    public ApiResponse<StatisticsResponse> statistics() {
        return ApiResponse.success(service.statistics(currentUserProvider.getCurrentUser()));
    }
}

