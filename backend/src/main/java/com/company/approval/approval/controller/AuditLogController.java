package com.company.approval.approval.controller;

import java.util.List;

import com.company.approval.approval.application.AuditLogApplicationService;
import com.company.approval.approval.dto.ApprovalActionLogResponse;
import com.company.approval.approval.dto.WorkflowConfigAuditResponse;
import com.company.approval.common.response.ApiResponse;
import com.company.approval.security.principal.CurrentUserProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogApplicationService service;
    private final CurrentUserProvider currentUserProvider;

    public AuditLogController(AuditLogApplicationService service, CurrentUserProvider currentUserProvider) {
        this.service = service;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/approval-actions")
    public ApiResponse<List<ApprovalActionLogResponse>> listApprovalActions() {
        return ApiResponse.success(service.listApprovalActions(currentUserProvider.getCurrentUser()));
    }

    @GetMapping("/workflow-configs")
    public ApiResponse<List<WorkflowConfigAuditResponse>> listWorkflowConfigAudits() {
        return ApiResponse.success(service.listWorkflowConfigAudits(currentUserProvider.getCurrentUser()));
    }
}
