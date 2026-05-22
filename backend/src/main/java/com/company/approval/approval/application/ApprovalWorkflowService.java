package com.company.approval.approval.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.company.approval.approval.domain.ApprovalActionLog;
import com.company.approval.approval.domain.ApprovalCc;
import com.company.approval.approval.domain.ApprovalRequest;
import com.company.approval.approval.domain.ApprovalTask;
import com.company.approval.approval.domain.ApprovalWorkflowInstance;
import com.company.approval.approval.domain.ApprovalWorkflowNodeInstance;
import com.company.approval.approval.domain.ApprovalWorkflowNodeTemplate;
import com.company.approval.approval.domain.ApprovalWorkflowTemplate;
import com.company.approval.approval.dto.ApprovalCcResponse;
import com.company.approval.approval.dto.ApprovalTaskResponse;
import com.company.approval.approval.repository.ApprovalActionLogRepository;
import com.company.approval.approval.repository.ApprovalCcRepository;
import com.company.approval.approval.repository.ApprovalRequestRepository;
import com.company.approval.approval.repository.ApprovalTaskRepository;
import com.company.approval.approval.repository.ApprovalWorkflowInstanceRepository;
import com.company.approval.approval.repository.ApprovalWorkflowNodeInstanceRepository;
import com.company.approval.approval.repository.ApprovalWorkflowNodeTemplateRepository;
import com.company.approval.approval.repository.ApprovalWorkflowTemplateRepository;
import com.company.approval.common.exception.BusinessException;
import com.company.approval.common.exception.ErrorCode;
import com.company.approval.notification.application.NotificationApplicationService;
import com.company.approval.security.principal.UserPrincipal;
import com.company.approval.system.domain.SysUser;
import com.company.approval.system.repository.SysUserRepository;
import com.company.approval.system.repository.SysUserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ApprovalWorkflowService {

    private final ApprovalRequestRepository requestRepository;
    private final ApprovalWorkflowInstanceRepository workflowRepository;
    private final ApprovalWorkflowNodeInstanceRepository nodeRepository;
    private final ApprovalWorkflowTemplateRepository templateRepository;
    private final ApprovalWorkflowNodeTemplateRepository nodeTemplateRepository;
    private final ApprovalTaskRepository taskRepository;
    private final ApprovalActionLogRepository actionLogRepository;
    private final ApprovalCcRepository ccRepository;
    private final NotificationApplicationService notificationService;
    private final SysUserRepository userRepository;
    private final SysUserRoleRepository userRoleRepository;

    public ApprovalWorkflowService(
            ApprovalRequestRepository requestRepository,
            ApprovalWorkflowInstanceRepository workflowRepository,
            ApprovalWorkflowNodeInstanceRepository nodeRepository,
            ApprovalWorkflowTemplateRepository templateRepository,
            ApprovalWorkflowNodeTemplateRepository nodeTemplateRepository,
            ApprovalTaskRepository taskRepository,
            ApprovalActionLogRepository actionLogRepository,
            ApprovalCcRepository ccRepository,
            NotificationApplicationService notificationService,
            SysUserRepository userRepository,
            SysUserRoleRepository userRoleRepository) {
        this.requestRepository = requestRepository;
        this.workflowRepository = workflowRepository;
        this.nodeRepository = nodeRepository;
        this.templateRepository = templateRepository;
        this.nodeTemplateRepository = nodeTemplateRepository;
        this.taskRepository = taskRepository;
        this.actionLogRepository = actionLogRepository;
        this.ccRepository = ccRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional
    public void startWorkflow(ApprovalRequest request, UserPrincipal principal) {
        if (workflowRepository.findByRequestIdAndDeletedFalse(request.getId()).isPresent()) {
            return;
        }
        ApprovalWorkflowInstance workflow = workflowRepository.save(new ApprovalWorkflowInstance(request.getId(), principal.getUserId()));
        List<NodeRule> rules = resolveRules(request);
        List<ApprovalWorkflowNodeInstance> nodes = new ArrayList<ApprovalWorkflowNodeInstance>();
        int sortOrder = 1;
        for (NodeRule rule : rules) {
            SysUser approver = resolveApprover(request, rule);
            nodes.add(nodeRepository.save(new ApprovalWorkflowNodeInstance(
                    workflow.getId(),
                    request.getId(),
                    rule.nodeName,
                    rule.approverRule,
                    approver.getId(),
                    approver.getDisplayName(),
                    sortOrder,
                    principal.getUserId())));
            sortOrder++;
        }
        activateNode(nodes.get(0), principal.getUserId());
    }

    @Transactional(readOnly = true)
    public List<ApprovalTaskResponse> listTodo(UserPrincipal principal) {
        return toTaskResponses(taskRepository.findByAssigneeIdAndStatusAndDeletedFalseOrderByAssignedAtDesc(principal.getUserId(), "pending"));
    }

    @Transactional(readOnly = true)
    public List<ApprovalTaskResponse> listDone(UserPrincipal principal) {
        return toTaskResponses(taskRepository.findByAssigneeIdAndStatusNotAndDeletedFalseOrderByActedAtDesc(principal.getUserId(), "pending"));
    }

    @Transactional(readOnly = true)
    public List<ApprovalCcResponse> listCc(UserPrincipal principal) {
        List<ApprovalCcResponse> responses = new ArrayList<ApprovalCcResponse>();
        for (ApprovalCc cc : ccRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(principal.getUserId())) {
            responses.add(new ApprovalCcResponse(cc, findRequest(cc.getRequestId())));
        }
        return responses;
    }

    @Transactional
    public ApprovalTaskResponse approve(Long taskId, String comment, UserPrincipal principal) {
        ApprovalTask task = findPendingTaskForAssignee(taskId, principal);
        ApprovalRequest request = findRequest(task.getRequestId());
        ApprovalWorkflowInstance workflow = findWorkflow(task.getWorkflowInstanceId());
        ApprovalWorkflowNodeInstance node = findNode(task.getNodeInstanceId());
        task.complete(comment, principal.getUserId());
        node.approve(principal.getUserId());
        actionLogRepository.save(new ApprovalActionLog(request.getId(), principal.getUserId(), principal.getDisplayName(), "approve", request.getStatus(), request.getStatus(), comment));
        ApprovalWorkflowNodeInstance nextNode = nodeRepository
                .findFirstByWorkflowInstanceIdAndSortOrderGreaterThanAndDeletedFalseOrderBySortOrderAsc(workflow.getId(), node.getSortOrder())
                .orElse(null);
        if (nextNode == null) {
            request.approve(principal.getUserId());
            workflow.complete("approved", principal.getUserId());
            notifyApplicant(request, "approved", "审批已通过", principal.getDisplayName() + " 已同意：" + request.getTitle(), task.getId(), principal.getUserId());
        } else {
            activateNode(nextNode, principal.getUserId());
        }
        return toTaskResponse(task);
    }

    @Transactional
    public ApprovalTaskResponse requestMoreInfo(Long taskId, String comment, UserPrincipal principal) {
        if (!StringUtils.hasText(comment)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Supplement comment is required");
        }
        ApprovalTask task = findPendingTaskForAssignee(taskId, principal);
        ApprovalRequest request = findRequest(task.getRequestId());
        ApprovalWorkflowNodeInstance node = findNode(task.getNodeInstanceId());
        String fromStatus = request.getStatus();
        task.finish("need_more_info", comment, principal.getUserId());
        node.requireMoreInfo(principal.getUserId());
        request.requireMoreInfo(principal.getUserId());
        actionLogRepository.save(new ApprovalActionLog(request.getId(), principal.getUserId(), principal.getDisplayName(), "request_more_info", fromStatus, "need_more_info", comment));
        notifyApplicant(request, "request_more_info", "请补充材料", principal.getDisplayName() + " 要求补充：" + comment, task.getId(), principal.getUserId());
        return toTaskResponse(task);
    }

    @Transactional
    public ApprovalTaskResponse transfer(Long taskId, Long targetUserId, String comment, UserPrincipal principal) {
        ApprovalTask task = findPendingTaskForAssignee(taskId, principal);
        SysUser target = requireActiveUser(targetUserId);
        ApprovalRequest request = findRequest(task.getRequestId());
        ApprovalWorkflowNodeInstance node = findNode(task.getNodeInstanceId());
        task.reassign(target.getId(), target.getDisplayName(), comment, principal.getUserId());
        node.transferApprover(target.getId(), target.getDisplayName(), principal.getUserId());
        actionLogRepository.save(new ApprovalActionLog(request.getId(), principal.getUserId(), principal.getDisplayName(), "transfer", request.getStatus(), request.getStatus(), target.getDisplayName() + appendComment(comment)));
        notificationService.create(target.getId(), "transfer", "审批任务已转交给你", request.getTitle(), request.getId(), task.getId(), principal.getUserId());
        return toTaskResponse(task);
    }

    @Transactional
    public ApprovalTaskResponse addSigner(Long taskId, Long targetUserId, String comment, UserPrincipal principal) {
        ApprovalTask task = findPendingTaskForAssignee(taskId, principal);
        SysUser target = requireActiveUser(targetUserId);
        ApprovalRequest request = findRequest(task.getRequestId());
        ApprovalWorkflowInstance workflow = findWorkflow(task.getWorkflowInstanceId());
        ApprovalWorkflowNodeInstance node = findNode(task.getNodeInstanceId());
        task.complete(comment, principal.getUserId());
        node.approve(principal.getUserId());
        for (ApprovalWorkflowNodeInstance existing : nodeRepository.findByWorkflowInstanceIdAndDeletedFalseOrderBySortOrderAsc(workflow.getId())) {
            if (existing.getSortOrder().compareTo(node.getSortOrder()) > 0) {
                existing.shiftSortOrder(1, principal.getUserId());
            }
        }
        ApprovalWorkflowNodeInstance signNode = nodeRepository.save(new ApprovalWorkflowNodeInstance(
                workflow.getId(),
                request.getId(),
                "加签审批",
                "add_signer",
                target.getId(),
                target.getDisplayName(),
                node.getSortOrder() + 1,
                principal.getUserId()));
        activateNode(signNode, principal.getUserId());
        actionLogRepository.save(new ApprovalActionLog(request.getId(), principal.getUserId(), principal.getDisplayName(), "add_signer", request.getStatus(), request.getStatus(), target.getDisplayName() + appendComment(comment)));
        notificationService.create(target.getId(), "add_signer", "你有一条加签审批", request.getTitle(), request.getId(), null, principal.getUserId());
        return toTaskResponse(task);
    }

    @Transactional
    public ApprovalTaskResponse cc(Long taskId, List<Long> targetUserIds, String comment, UserPrincipal principal) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "CC user is required");
        }
        ApprovalTask task = findPendingTaskForAssignee(taskId, principal);
        ApprovalRequest request = findRequest(task.getRequestId());
        List<String> names = new ArrayList<String>();
        for (Long targetUserId : targetUserIds) {
            SysUser target = requireActiveUser(targetUserId);
            ccRepository.save(new ApprovalCc(request.getId(), target.getId(), target.getDisplayName(), comment, principal.getUserId()));
            notificationService.create(target.getId(), "cc", "你收到一条审批抄送", request.getTitle(), request.getId(), task.getId(), principal.getUserId());
            names.add(target.getDisplayName());
        }
        actionLogRepository.save(new ApprovalActionLog(request.getId(), principal.getUserId(), principal.getDisplayName(), "cc", request.getStatus(), request.getStatus(), StringUtils.collectionToCommaDelimitedString(names) + appendComment(comment)));
        return toTaskResponse(task);
    }

    @Transactional
    public void withdraw(Long requestId, String comment, UserPrincipal principal) {
        ApprovalRequest request = findRequest(requestId);
        if (!principal.getUserId().equals(request.getApplicantId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!"in_progress".equals(request.getStatus()) && !"need_more_info".equals(request.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Only in-progress request can be withdrawn");
        }
        String fromStatus = request.getStatus();
        request.withdraw(principal.getUserId());
        ApprovalWorkflowInstance workflow = workflowRepository.findByRequestIdAndDeletedFalse(request.getId()).orElse(null);
        if (workflow != null) {
            workflow.complete("withdrawn", principal.getUserId());
        }
        for (ApprovalTask task : taskRepository.findByRequestIdAndStatusAndDeletedFalse(request.getId(), "pending")) {
            task.finish("withdrawn", comment, principal.getUserId());
            ApprovalWorkflowNodeInstance node = findNode(task.getNodeInstanceId());
            node.withdraw(principal.getUserId());
            notificationService.create(task.getAssigneeId(), "withdraw", "审批申请已撤回", request.getTitle(), request.getId(), task.getId(), principal.getUserId());
        }
        actionLogRepository.save(new ApprovalActionLog(request.getId(), principal.getUserId(), principal.getDisplayName(), "withdraw", fromStatus, "withdrawn", comment));
    }

    @Transactional
    public void voidRequest(Long requestId, String comment, UserPrincipal principal) {
        if (!principal.getRoles().contains("admin")) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        ApprovalRequest request = findRequest(requestId);
        if ("approved".equals(request.getStatus()) || "rejected".equals(request.getStatus()) || "withdrawn".equals(request.getStatus()) || "voided".equals(request.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Completed request cannot be voided");
        }
        String fromStatus = request.getStatus();
        request.voidRequest(principal.getUserId());
        ApprovalWorkflowInstance workflow = workflowRepository.findByRequestIdAndDeletedFalse(request.getId()).orElse(null);
        if (workflow != null) {
            workflow.complete("voided", principal.getUserId());
        }
        for (ApprovalTask task : taskRepository.findByRequestIdAndStatusAndDeletedFalse(request.getId(), "pending")) {
            task.finish("voided", comment, principal.getUserId());
            ApprovalWorkflowNodeInstance node = findNode(task.getNodeInstanceId());
            node.voidNode(principal.getUserId());
            notificationService.create(task.getAssigneeId(), "voided", "审批申请已作废", request.getTitle(), request.getId(), task.getId(), principal.getUserId());
        }
        actionLogRepository.save(new ApprovalActionLog(request.getId(), principal.getUserId(), principal.getDisplayName(), "void", fromStatus, "voided", comment));
        notifyApplicant(request, "voided", "审批申请已作废", request.getTitle(), null, principal.getUserId());
    }

    @Transactional
    public void resubmitAfterMoreInfo(ApprovalRequest request, String comment, UserPrincipal principal) {
        if (!principal.getUserId().equals(request.getApplicantId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!"need_more_info".equals(request.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Only request needing more info can be resubmitted");
        }
        ApprovalWorkflowInstance workflow = workflowRepository.findByRequestIdAndDeletedFalse(request.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Workflow instance not found"));
        ApprovalWorkflowNodeInstance node = nodeRepository
                .findFirstByWorkflowInstanceIdAndStatusAndDeletedFalseOrderBySortOrderAsc(workflow.getId(), "need_more_info")
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Workflow node not found"));
        request.resubmit(principal.getUserId());
        node.reactivate(principal.getUserId());
        ApprovalTask task = taskRepository.save(new ApprovalTask(node.getRequestId(), node.getWorkflowInstanceId(), node.getId(), node.getApproverId(), node.getApproverName(), principal.getUserId()));
        notificationService.create(task.getAssigneeId(), "resubmit_more_info", "补充材料已提交", request.getTitle(), request.getId(), task.getId(), principal.getUserId());
        actionLogRepository.save(new ApprovalActionLog(request.getId(), principal.getUserId(), principal.getDisplayName(), "resubmit_more_info", "need_more_info", "in_progress", comment));
    }

    @Transactional
    public ApprovalTaskResponse reject(Long taskId, String comment, UserPrincipal principal) {
        if (!StringUtils.hasText(comment)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Reject comment is required");
        }
        ApprovalTask task = findPendingTaskForAssignee(taskId, principal);
        ApprovalRequest request = findRequest(task.getRequestId());
        ApprovalWorkflowInstance workflow = findWorkflow(task.getWorkflowInstanceId());
        ApprovalWorkflowNodeInstance node = findNode(task.getNodeInstanceId());
        task.complete(comment, principal.getUserId());
        node.reject(principal.getUserId());
        request.reject(principal.getUserId());
        workflow.complete("rejected", principal.getUserId());
        actionLogRepository.save(new ApprovalActionLog(request.getId(), principal.getUserId(), principal.getDisplayName(), "reject", "in_progress", "rejected", comment));
        notifyApplicant(request, "rejected", "审批已驳回", principal.getDisplayName() + " 已驳回：" + comment, task.getId(), principal.getUserId());
        return toTaskResponse(task);
    }

    private void activateNode(ApprovalWorkflowNodeInstance node, Long operatorId) {
        node.activate(operatorId);
        ApprovalTask task = taskRepository.save(new ApprovalTask(node.getRequestId(), node.getWorkflowInstanceId(), node.getId(), node.getApproverId(), node.getApproverName(), operatorId));
        ApprovalRequest request = findRequest(node.getRequestId());
        notificationService.create(task.getAssigneeId(), "todo", "你有新的审批待办", request.getTitle(), request.getId(), task.getId(), operatorId);
    }

    private List<ApprovalTaskResponse> toTaskResponses(List<ApprovalTask> tasks) {
        List<ApprovalTaskResponse> responses = new ArrayList<ApprovalTaskResponse>();
        for (ApprovalTask task : tasks) {
            responses.add(toTaskResponse(task));
        }
        return responses;
    }

    private ApprovalTaskResponse toTaskResponse(ApprovalTask task) {
        ApprovalRequest request = findRequest(task.getRequestId());
        ApprovalWorkflowNodeInstance node = nodeRepository.findById(task.getNodeInstanceId()).orElse(null);
        return new ApprovalTaskResponse(task, request, node);
    }

    private ApprovalTask findPendingTaskForAssignee(Long taskId, UserPrincipal principal) {
        ApprovalTask task = taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Approval task not found"));
        if (!"pending".equals(task.getStatus()) || !principal.getUserId().equals(task.getAssigneeId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return task;
    }

    private ApprovalRequest findRequest(Long requestId) {
        ApprovalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Approval request not found"));
        if (Boolean.TRUE.equals(request.getDeleted())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Approval request not found");
        }
        return request;
    }

    private ApprovalWorkflowInstance findWorkflow(Long workflowId) {
        return workflowRepository.findById(workflowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Workflow instance not found"));
    }

    private ApprovalWorkflowNodeInstance findNode(Long nodeId) {
        return nodeRepository.findById(nodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Workflow node not found"));
    }

    private List<NodeRule> resolveRules(ApprovalRequest request) {
        ApprovalWorkflowTemplate template = templateRepository.findFirstByApprovalTypeAndEnabledTrueAndDeletedFalseOrderByUpdatedAtDesc(request.getType()).orElse(null);
        if (template != null) {
            List<NodeRule> configuredRules = new ArrayList<NodeRule>();
            for (ApprovalWorkflowNodeTemplate node : nodeTemplateRepository.findByTemplateIdAndDeletedFalseOrderBySortOrderAsc(template.getId())) {
                if (!Boolean.TRUE.equals(node.getEnabled())) {
                    continue;
                }
                if (node.getAmountGreaterThan() != null && (request.getAmount() == null || request.getAmount().compareTo(node.getAmountGreaterThan()) <= 0)) {
                    continue;
                }
                configuredRules.add(new NodeRule(node.getNodeName(), node.getApproverRule(), node.getApproverUserId()));
            }
            if (!configuredRules.isEmpty()) {
                return configuredRules;
            }
        }
        List<NodeRule> rules = new ArrayList<NodeRule>();
        if ("leave".equals(request.getType()) || "overtime".equals(request.getType()) || "business_trip".equals(request.getType())) {
            rules.add(new NodeRule("主管审批", "direct_manager"));
        } else if ("expense".equals(request.getType())) {
            rules.add(new NodeRule("主管审批", "direct_manager"));
            rules.add(new NodeRule("财务审批", "finance"));
            if (request.getAmount() != null && request.getAmount().compareTo(new BigDecimal("1000")) > 0) {
                rules.add(new NodeRule("总经理审批", "general_manager"));
            }
        } else if ("purchase".equals(request.getType())) {
            rules.add(new NodeRule("主管审批", "direct_manager"));
            rules.add(new NodeRule("总经理审批", "general_manager"));
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported approval type");
        }
        return rules;
    }

    private SysUser resolveApprover(ApprovalRequest request, NodeRule rule) {
        if ("specified_user".equals(rule.approverRule)) {
            SysUser user = findActiveUser(rule.approverUserId);
            if (user == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Specified approver not found");
            }
            return user;
        }
        if ("direct_manager".equals(rule.approverRule)) {
            SysUser applicant = userRepository.findById(request.getApplicantId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Applicant not found"));
            if (applicant.getDirectManagerId() != null) {
                SysUser manager = findActiveUser(applicant.getDirectManagerId());
                if (manager != null) {
                    return manager;
                }
            }
            return findFirstUserByRole("admin", request.getApplicantId());
        }
        SysUser roleUser = findFirstUserByRole(rule.approverRule, request.getApplicantId());
        if (roleUser != null) {
            return roleUser;
        }
        return findFirstUserByRole("admin", request.getApplicantId());
    }

    private SysUser findFirstUserByRole(String roleCode, Long fallbackUserId) {
        for (Long userId : userRoleRepository.findUserIdsByRoleCode(roleCode)) {
            SysUser user = findActiveUser(userId);
            if (user != null) {
                return user;
            }
        }
        return fallbackUserId == null ? null : findActiveUser(fallbackUserId);
    }

    private SysUser findActiveUser(Long userId) {
        SysUser user = userRepository.findById(userId).orElse(null);
        if (user == null || Boolean.TRUE.equals(user.getDeleted()) || !"active".equals(user.getStatus())) {
            return null;
        }
        return user;
    }

    private SysUser requireActiveUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Target user is required");
        }
        SysUser user = findActiveUser(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Target user not found");
        }
        return user;
    }

    private String appendComment(String comment) {
        return StringUtils.hasText(comment) ? "：" + comment : "";
    }

    private void notifyApplicant(ApprovalRequest request, String type, String title, String content, Long taskId, Long operatorId) {
        notificationService.create(request.getApplicantId(), type, title, content, request.getId(), taskId, operatorId);
    }

    private static class NodeRule {
        private final String nodeName;
        private final String approverRule;
        private final Long approverUserId;

        private NodeRule(String nodeName, String approverRule) {
            this(nodeName, approverRule, null);
        }

        private NodeRule(String nodeName, String approverRule, Long approverUserId) {
            this.nodeName = nodeName;
            this.approverRule = approverRule;
            this.approverUserId = approverUserId;
        }
    }
}
