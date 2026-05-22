package com.company.approval.approval.application;

import java.util.ArrayList;
import java.util.List;

import com.company.approval.approval.domain.ApprovalWorkflowConfigAudit;
import com.company.approval.approval.domain.ApprovalWorkflowNodeTemplate;
import com.company.approval.approval.domain.ApprovalWorkflowTemplate;
import com.company.approval.approval.dto.WorkflowConfigAuditResponse;
import com.company.approval.approval.dto.WorkflowNodeTemplateCommand;
import com.company.approval.approval.dto.WorkflowTemplateCommand;
import com.company.approval.approval.dto.WorkflowTemplateResponse;
import com.company.approval.approval.repository.ApprovalWorkflowConfigAuditRepository;
import com.company.approval.approval.repository.ApprovalWorkflowNodeTemplateRepository;
import com.company.approval.approval.repository.ApprovalWorkflowTemplateRepository;
import com.company.approval.common.exception.BusinessException;
import com.company.approval.common.exception.ErrorCode;
import com.company.approval.security.principal.UserPrincipal;
import com.company.approval.system.domain.SysUser;
import com.company.approval.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class WorkflowTemplateApplicationService {

    private final ApprovalWorkflowTemplateRepository templateRepository;
    private final ApprovalWorkflowNodeTemplateRepository nodeRepository;
    private final ApprovalWorkflowConfigAuditRepository auditRepository;
    private final SysUserRepository userRepository;

    public WorkflowTemplateApplicationService(
            ApprovalWorkflowTemplateRepository templateRepository,
            ApprovalWorkflowNodeTemplateRepository nodeRepository,
            ApprovalWorkflowConfigAuditRepository auditRepository,
            SysUserRepository userRepository) {
        this.templateRepository = templateRepository;
        this.nodeRepository = nodeRepository;
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<WorkflowTemplateResponse> list(UserPrincipal principal) {
        requireAdmin(principal);
        List<WorkflowTemplateResponse> responses = new ArrayList<WorkflowTemplateResponse>();
        for (ApprovalWorkflowTemplate template : templateRepository.findByDeletedFalseOrderByApprovalTypeAscIdAsc()) {
            responses.add(toResponse(template));
        }
        return responses;
    }

    @Transactional
    public WorkflowTemplateResponse create(WorkflowTemplateCommand command, UserPrincipal principal) {
        requireAdmin(principal);
        validate(command);
        ApprovalWorkflowTemplate template = templateRepository.save(new ApprovalWorkflowTemplate(command.getApprovalType(), command.getName(), principal.getUserId()));
        template.update(command.getApprovalType(), command.getName(), command.getEnabled(), principal.getUserId());
        replaceNodes(template.getId(), command.getNodes(), principal.getUserId());
        auditRepository.save(new ApprovalWorkflowConfigAudit(template.getId(), principal.getUserId(), principal.getDisplayName(), "create", template.getName()));
        return toResponse(template);
    }

    @Transactional
    public WorkflowTemplateResponse update(Long id, WorkflowTemplateCommand command, UserPrincipal principal) {
        requireAdmin(principal);
        validate(command);
        ApprovalWorkflowTemplate template = findTemplate(id);
        template.update(command.getApprovalType(), command.getName(), command.getEnabled(), principal.getUserId());
        replaceNodes(template.getId(), command.getNodes(), principal.getUserId());
        auditRepository.save(new ApprovalWorkflowConfigAudit(template.getId(), principal.getUserId(), principal.getDisplayName(), "update", template.getName()));
        return toResponse(template);
    }

    @Transactional
    public WorkflowTemplateResponse setEnabled(Long id, Boolean enabled, UserPrincipal principal) {
        requireAdmin(principal);
        ApprovalWorkflowTemplate template = findTemplate(id);
        template.setEnabled(Boolean.TRUE.equals(enabled), principal.getUserId());
        auditRepository.save(new ApprovalWorkflowConfigAudit(template.getId(), principal.getUserId(), principal.getDisplayName(), Boolean.TRUE.equals(enabled) ? "enable" : "disable", template.getName()));
        return toResponse(template);
    }

    @Transactional(readOnly = true)
    public List<WorkflowConfigAuditResponse> listAudits(UserPrincipal principal) {
        requireAdmin(principal);
        List<WorkflowConfigAuditResponse> responses = new ArrayList<WorkflowConfigAuditResponse>();
        for (ApprovalWorkflowConfigAudit audit : auditRepository.findByDeletedFalseOrderByCreatedAtDesc()) {
            responses.add(new WorkflowConfigAuditResponse(audit));
        }
        return responses;
    }

    private void replaceNodes(Long templateId, List<WorkflowNodeTemplateCommand> nodes, Long operatorId) {
        for (ApprovalWorkflowNodeTemplate node : nodeRepository.findByTemplateIdAndDeletedFalseOrderBySortOrderAsc(templateId)) {
            node.softDelete(operatorId);
        }
        for (WorkflowNodeTemplateCommand node : nodes) {
            nodeRepository.save(new ApprovalWorkflowNodeTemplate(
                    templateId,
                    node.getNodeName(),
                    node.getApproverRule(),
                    node.getApproverUserId(),
                    node.getAmountGreaterThan(),
                    node.getSortOrder(),
                    operatorId));
        }
    }

    private void validate(WorkflowTemplateCommand command) {
        if (command.getNodes() == null || command.getNodes().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Workflow node is required");
        }
        for (WorkflowNodeTemplateCommand node : command.getNodes()) {
            if (!StringUtils.hasText(node.getNodeName()) || !StringUtils.hasText(node.getApproverRule()) || node.getSortOrder() == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid workflow node");
            }
            if ("specified_user".equals(node.getApproverRule())) {
                SysUser user = node.getApproverUserId() == null ? null : userRepository.findById(node.getApproverUserId()).orElse(null);
                if (user == null || Boolean.TRUE.equals(user.getDeleted()) || !"active".equals(user.getStatus())) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "Specified approver is required");
                }
            }
        }
    }

    private WorkflowTemplateResponse toResponse(ApprovalWorkflowTemplate template) {
        return new WorkflowTemplateResponse(template, nodeRepository.findByTemplateIdAndDeletedFalseOrderBySortOrderAsc(template.getId()));
    }

    private ApprovalWorkflowTemplate findTemplate(Long id) {
        ApprovalWorkflowTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Workflow template not found"));
        return template;
    }

    private void requireAdmin(UserPrincipal principal) {
        if (!principal.getRoles().contains("admin")) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
