package com.company.approval.approval.controller;

import java.util.List;
import javax.validation.Valid;

import com.company.approval.approval.application.WorkflowTemplateApplicationService;
import com.company.approval.approval.dto.WorkflowConfigAuditResponse;
import com.company.approval.approval.dto.WorkflowTemplateCommand;
import com.company.approval.approval.dto.WorkflowTemplateResponse;
import com.company.approval.common.response.ApiResponse;
import com.company.approval.security.principal.CurrentUserProvider;
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
@RequestMapping("/api/workflow-templates")
public class WorkflowTemplateController {

    private final WorkflowTemplateApplicationService service;
    private final CurrentUserProvider currentUserProvider;

    public WorkflowTemplateController(WorkflowTemplateApplicationService service, CurrentUserProvider currentUserProvider) {
        this.service = service;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ApiResponse<List<WorkflowTemplateResponse>> list() {
        return ApiResponse.success(service.list(currentUserProvider.getCurrentUser()));
    }

    @PostMapping
    public ApiResponse<WorkflowTemplateResponse> create(@Valid @RequestBody WorkflowTemplateCommand command) {
        return ApiResponse.success(service.create(command, currentUserProvider.getCurrentUser()));
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkflowTemplateResponse> update(@PathVariable Long id, @Valid @RequestBody WorkflowTemplateCommand command) {
        return ApiResponse.success(service.update(id, command, currentUserProvider.getCurrentUser()));
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<WorkflowTemplateResponse> setEnabled(@PathVariable Long id, @RequestParam Boolean enabled) {
        return ApiResponse.success(service.setEnabled(id, enabled, currentUserProvider.getCurrentUser()));
    }

    @GetMapping("/audits")
    public ApiResponse<List<WorkflowConfigAuditResponse>> listAudits() {
        return ApiResponse.success(service.listAudits(currentUserProvider.getCurrentUser()));
    }
}
