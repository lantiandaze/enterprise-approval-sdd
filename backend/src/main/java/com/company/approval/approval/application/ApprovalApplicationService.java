package com.company.approval.approval.application;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.company.approval.approval.domain.ApprovalActionLog;
import com.company.approval.approval.domain.ApprovalAttachment;
import com.company.approval.approval.domain.ApprovalRequest;
import com.company.approval.approval.domain.ApprovalTask;
import com.company.approval.approval.domain.ApprovalWorkflowNodeInstance;
import com.company.approval.approval.dto.ApprovalActionLogResponse;
import com.company.approval.approval.dto.ApprovalAttachmentResponse;
import com.company.approval.approval.dto.ApprovalRequestCommand;
import com.company.approval.approval.dto.ApprovalRequestResponse;
import com.company.approval.approval.dto.ApprovalTimelineNodeResponse;
import com.company.approval.approval.repository.ApprovalActionLogRepository;
import com.company.approval.approval.repository.ApprovalAttachmentRepository;
import com.company.approval.approval.repository.ApprovalCcRepository;
import com.company.approval.approval.repository.ApprovalRequestRepository;
import com.company.approval.approval.repository.ApprovalTaskRepository;
import com.company.approval.approval.repository.ApprovalWorkflowNodeInstanceRepository;
import com.company.approval.common.exception.BusinessException;
import com.company.approval.common.exception.ErrorCode;
import com.company.approval.organization.domain.OrgDepartment;
import com.company.approval.organization.repository.OrgDepartmentRepository;
import com.company.approval.security.principal.UserPrincipal;
import com.company.approval.system.domain.SysUser;
import com.company.approval.system.repository.SysUserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovalApplicationService {

    private final ApprovalRequestRepository requestRepository;
    private final ApprovalActionLogRepository actionLogRepository;
    private final ApprovalAttachmentRepository attachmentRepository;
    private final ApprovalWorkflowNodeInstanceRepository nodeRepository;
    private final ApprovalTaskRepository taskRepository;
    private final ApprovalCcRepository ccRepository;
    private final SysUserRepository userRepository;
    private final OrgDepartmentRepository departmentRepository;
    private final ApprovalFormValidator validator;
    private final ApprovalWorkflowService workflowService;
    private final ObjectMapper objectMapper;

    public ApprovalApplicationService(
            ApprovalRequestRepository requestRepository,
            ApprovalActionLogRepository actionLogRepository,
            ApprovalAttachmentRepository attachmentRepository,
            ApprovalWorkflowNodeInstanceRepository nodeRepository,
            ApprovalTaskRepository taskRepository,
            ApprovalCcRepository ccRepository,
            SysUserRepository userRepository,
            OrgDepartmentRepository departmentRepository,
            ApprovalFormValidator validator,
            ApprovalWorkflowService workflowService,
            ObjectMapper objectMapper) {
        this.requestRepository = requestRepository;
        this.actionLogRepository = actionLogRepository;
        this.attachmentRepository = attachmentRepository;
        this.nodeRepository = nodeRepository;
        this.taskRepository = taskRepository;
        this.ccRepository = ccRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.validator = validator;
        this.workflowService = workflowService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ApprovalRequestResponse saveDraft(ApprovalRequestCommand command, UserPrincipal principal) {
        validator.validateDraft(command);
        ApprovalRequest request = createRequest(principal);
        updateRequest(request, command, principal.getUserId());
        ApprovalRequest saved = requestRepository.save(request);
        actionLogRepository.save(new ApprovalActionLog(saved.getId(), principal.getUserId(), principal.getDisplayName(), "save_draft", null, "draft", null));
        return toResponse(saved, true);
    }

    @Transactional
    public ApprovalRequestResponse submitNew(ApprovalRequestCommand command, UserPrincipal principal) {
        validator.validateSubmit(command);
        if (requiresAttachment(command.getType())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Please save draft and upload attachment before submitting this approval type");
        }
        ApprovalRequest request = createRequest(principal);
        updateRequest(request, command, principal.getUserId());
        request.submit(principal.getUserId());
        ApprovalRequest saved = requestRepository.save(request);
        actionLogRepository.save(new ApprovalActionLog(saved.getId(), principal.getUserId(), principal.getDisplayName(), "submit", "draft", "in_progress", null));
        workflowService.startWorkflow(saved, principal);
        return toResponse(saved, true);
    }

    @Transactional
    public ApprovalRequestResponse submitDraft(Long id, ApprovalRequestCommand command, UserPrincipal principal) {
        validator.validateSubmit(command);
        ApprovalRequest request = findOwnedRequest(id, principal);
        if (!"draft".equals(request.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Only draft can be submitted");
        }
        updateRequest(request, command, principal.getUserId());
        validateRequiredAttachment(request);
        request.submit(principal.getUserId());
        actionLogRepository.save(new ApprovalActionLog(request.getId(), principal.getUserId(), principal.getDisplayName(), "submit", "draft", "in_progress", null));
        workflowService.startWorkflow(request, principal);
        return toResponse(request, true);
    }

    @Transactional
    public ApprovalRequestResponse resubmitMoreInfo(Long id, ApprovalRequestCommand command, String comment, UserPrincipal principal) {
        validator.validateSubmit(command);
        ApprovalRequest request = findOwnedRequest(id, principal);
        if (!"need_more_info".equals(request.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Only request needing more info can be resubmitted");
        }
        updateRequest(request, command, principal.getUserId());
        validateRequiredAttachment(request);
        workflowService.resubmitAfterMoreInfo(request, comment, principal);
        return toResponse(request, true);
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> listMine(UserPrincipal principal) {
        List<ApprovalRequestResponse> responses = new ArrayList<ApprovalRequestResponse>();
        for (ApprovalRequest request : requestRepository.findByApplicantIdAndDeletedFalseOrderByCreatedAtDesc(principal.getUserId())) {
            responses.add(toResponse(request, false));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public ApprovalRequestResponse getMine(Long id, UserPrincipal principal) {
        return toResponse(findVisibleRequest(id, principal), true);
    }

    private ApprovalRequest createRequest(UserPrincipal principal) {
        SysUser user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "User not found"));
        String departmentName = null;
        if (user.getDepartmentId() != null) {
            OrgDepartment department = departmentRepository.findById(user.getDepartmentId()).orElse(null);
            departmentName = department == null ? null : department.getName();
        }
        return new ApprovalRequest(nextRequestNo(), principal.getUserId(), principal.getDisplayName(), user.getDepartmentId(), departmentName);
    }

    private void updateRequest(ApprovalRequest request, ApprovalRequestCommand command, Long operatorId) {
        request.updateDraft(
                command.getTitle(),
                command.getType(),
                command.getUrgent(),
                command.getAmount(),
                command.getStartTime(),
                command.getEndTime(),
                toJson(command.getFormData()),
                operatorId);
    }

    private ApprovalRequest findOwnedRequest(Long id, UserPrincipal principal) {
        ApprovalRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Approval request not found"));
        if (Boolean.TRUE.equals(request.getDeleted()) || !principal.getUserId().equals(request.getApplicantId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return request;
    }

    private ApprovalRequest findVisibleRequest(Long id, UserPrincipal principal) {
        ApprovalRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Approval request not found"));
        if (Boolean.TRUE.equals(request.getDeleted())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Approval request not found");
        }
        if (principal.getUserId().equals(request.getApplicantId())
                || principal.getRoles().contains("admin")
                || taskRepository.existsByRequestIdAndAssigneeIdAndDeletedFalse(request.getId(), principal.getUserId())
                || ccRepository.existsByRequestIdAndUserIdAndDeletedFalse(request.getId(), principal.getUserId())) {
            return request;
        }
        throw new BusinessException(ErrorCode.FORBIDDEN);
    }

    private void validateRequiredAttachment(ApprovalRequest request) {
        if (requiresAttachment(request.getType()) && attachmentRepository.findByRequestIdAndDeletedFalseOrderByCreatedAtAsc(request.getId()).isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Attachment is required for this approval type");
        }
    }

    private boolean requiresAttachment(String type) {
        return "expense".equals(type) || "purchase".equals(type);
    }

    private ApprovalRequestResponse toResponse(ApprovalRequest request, boolean includeLogs) {
        List<ApprovalActionLogResponse> logs = new ArrayList<ApprovalActionLogResponse>();
        if (includeLogs) {
            for (ApprovalActionLog log : actionLogRepository.findByRequestIdAndDeletedFalseOrderByCreatedAtAsc(request.getId())) {
                logs.add(new ApprovalActionLogResponse(log));
            }
        }
        List<ApprovalAttachmentResponse> attachments = new ArrayList<ApprovalAttachmentResponse>();
        for (ApprovalAttachment attachment : attachmentRepository.findByRequestIdAndDeletedFalseOrderByCreatedAtAsc(request.getId())) {
            attachments.add(new ApprovalAttachmentResponse(attachment));
        }
        List<ApprovalTimelineNodeResponse> timelineNodes = new ArrayList<ApprovalTimelineNodeResponse>();
        for (ApprovalWorkflowNodeInstance node : nodeRepository.findByRequestIdAndDeletedFalseOrderBySortOrderAsc(request.getId())) {
            ApprovalTask task = taskRepository.findFirstByNodeInstanceIdAndDeletedFalseOrderByAssignedAtDesc(node.getId()).orElse(null);
            timelineNodes.add(new ApprovalTimelineNodeResponse(node, task));
        }
        return new ApprovalRequestResponse(request, fromJson(request.getFormData()), logs, attachments, timelineNodes);
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid form data");
        }
    }

    private Map<String, Object> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Invalid stored form data");
        }
    }

    private String nextRequestNo() {
        return "AP" + DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(OffsetDateTime.now());
    }
}
