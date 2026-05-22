package com.company.approval.approval.application;

import java.util.ArrayList;
import java.util.List;

import com.company.approval.approval.domain.ApprovalActionLog;
import com.company.approval.approval.domain.ApprovalWorkflowConfigAudit;
import com.company.approval.approval.dto.ApprovalActionLogResponse;
import com.company.approval.approval.dto.WorkflowConfigAuditResponse;
import com.company.approval.approval.repository.ApprovalActionLogRepository;
import com.company.approval.approval.repository.ApprovalWorkflowConfigAuditRepository;
import com.company.approval.common.exception.BusinessException;
import com.company.approval.common.exception.ErrorCode;
import com.company.approval.security.principal.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogApplicationService {

    private final ApprovalActionLogRepository actionLogRepository;
    private final ApprovalWorkflowConfigAuditRepository workflowConfigAuditRepository;

    public AuditLogApplicationService(
            ApprovalActionLogRepository actionLogRepository,
            ApprovalWorkflowConfigAuditRepository workflowConfigAuditRepository) {
        this.actionLogRepository = actionLogRepository;
        this.workflowConfigAuditRepository = workflowConfigAuditRepository;
    }

    @Transactional(readOnly = true)
    public List<ApprovalActionLogResponse> listApprovalActions(UserPrincipal principal) {
        requireAdmin(principal);
        List<ApprovalActionLogResponse> responses = new ArrayList<ApprovalActionLogResponse>();
        for (ApprovalActionLog log : actionLogRepository.findByDeletedFalseOrderByCreatedAtDesc()) {
            responses.add(new ApprovalActionLogResponse(log));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<WorkflowConfigAuditResponse> listWorkflowConfigAudits(UserPrincipal principal) {
        requireAdmin(principal);
        List<WorkflowConfigAuditResponse> responses = new ArrayList<WorkflowConfigAuditResponse>();
        for (ApprovalWorkflowConfigAudit audit : workflowConfigAuditRepository.findByDeletedFalseOrderByCreatedAtDesc()) {
            responses.add(new WorkflowConfigAuditResponse(audit));
        }
        return responses;
    }

    private void requireAdmin(UserPrincipal principal) {
        if (!principal.getRoles().contains("admin")) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
