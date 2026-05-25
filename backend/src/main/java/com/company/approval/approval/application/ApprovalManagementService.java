package com.company.approval.approval.application;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.Predicate;

import com.company.approval.approval.domain.ApprovalActionLog;
import com.company.approval.approval.domain.ApprovalRequest;
import com.company.approval.approval.dto.ApprovalManagementQuery;
import com.company.approval.approval.dto.ApprovalManagementResponse;
import com.company.approval.approval.dto.DashboardSummaryResponse;
import com.company.approval.approval.dto.StatisticsResponse;
import com.company.approval.approval.repository.ApprovalActionLogRepository;
import com.company.approval.approval.repository.ApprovalRequestRepository;
import com.company.approval.approval.repository.ApprovalTaskRepository;
import com.company.approval.common.exception.BusinessException;
import com.company.approval.common.exception.ErrorCode;
import com.company.approval.common.pagination.PagedResult;
import com.company.approval.notification.repository.NotificationRepository;
import com.company.approval.security.permission.DataPermissionDecision;
import com.company.approval.security.permission.DataPermissionScope;
import com.company.approval.security.permission.DataPermissionService;
import com.company.approval.security.principal.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ApprovalManagementService {

    private static final int EXPORT_HARD_LIMIT = 5000;

    private final ApprovalRequestRepository requestRepository;
    private final ApprovalTaskRepository taskRepository;
    private final ApprovalActionLogRepository actionLogRepository;
    private final NotificationRepository notificationRepository;
    private final DataPermissionService dataPermissionService;

    public ApprovalManagementService(
            ApprovalRequestRepository requestRepository,
            ApprovalTaskRepository taskRepository,
            ApprovalActionLogRepository actionLogRepository,
            NotificationRepository notificationRepository,
            DataPermissionService dataPermissionService) {
        this.requestRepository = requestRepository;
        this.taskRepository = taskRepository;
        this.actionLogRepository = actionLogRepository;
        this.notificationRepository = notificationRepository;
        this.dataPermissionService = dataPermissionService;
    }

    @Transactional(readOnly = true)
    public PagedResult<ApprovalManagementResponse> list(ApprovalManagementQuery query, UserPrincipal principal) {
        int page = query.resolvePage();
        int pageSize = query.resolvePageSize();
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ApprovalRequest> resultPage = requestRepository.findAll(buildSpec(query, principal), pageable);
        List<ApprovalManagementResponse> responses = new ArrayList<ApprovalManagementResponse>();
        for (ApprovalRequest request : resultPage.getContent()) {
            responses.add(new ApprovalManagementResponse(request));
        }
        return new PagedResult<ApprovalManagementResponse>(responses, resultPage.getTotalElements(), page, pageSize);
    }

    @Transactional
    public byte[] exportCsv(ApprovalManagementQuery query, UserPrincipal principal) {
        if (!principal.getRoles().contains("admin") && !principal.getRoles().contains("general_manager")) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        Pageable pageable = PageRequest.of(0, EXPORT_HARD_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ApprovalRequest> resultPage = requestRepository.findAll(buildSpec(query, principal), pageable);
        List<ApprovalRequest> requests = resultPage.getContent();
        StringBuilder csv = new StringBuilder();
        csv.append("requestNo,title,type,status,applicantName,departmentName,amount,urgent,submittedAt,createdAt\n");
        for (ApprovalRequest request : requests) {
            csv.append(csv(request.getRequestNo())).append(',')
                    .append(csv(request.getTitle())).append(',')
                    .append(csv(request.getType())).append(',')
                    .append(csv(request.getStatus())).append(',')
                    .append(csv(request.getApplicantName())).append(',')
                    .append(csv(request.getDepartmentName())).append(',')
                    .append(request.getAmount() == null ? "" : request.getAmount()).append(',')
                    .append(Boolean.TRUE.equals(request.getUrgent())).append(',')
                    .append(request.getSubmittedAt() == null ? "" : request.getSubmittedAt()).append(',')
                    .append(request.getCreatedAt() == null ? "" : request.getCreatedAt()).append('\n');
        }
        Long anchorRequestId = requests.isEmpty() ? null : requests.get(0).getId();
        String anchorStatus = requests.isEmpty() ? "approved" : requests.get(0).getStatus();
        if (anchorRequestId != null) {
            actionLogRepository.save(new ApprovalActionLog(
                    anchorRequestId,
                    principal.getUserId(),
                    principal.getDisplayName(),
                    "export",
                    anchorStatus,
                    anchorStatus,
                    "导出审批记录：" + requests.size() + " 条，total=" + resultPage.getTotalElements()));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse dashboard(UserPrincipal principal) {
        long myTodo = taskRepository.countByAssigneeIdAndStatusAndDeletedFalse(principal.getUserId(), "pending");
        long myInProgress = requestRepository.count((root, query, cb) -> cb.and(
                cb.isFalse(root.get("deleted")),
                cb.equal(root.get("applicantId"), principal.getUserId()),
                cb.equal(root.get("status"), "in_progress")));
        long unread = notificationRepository.countByUserIdAndReadAtIsNullAndDeletedFalse(principal.getUserId());
        DataPermissionDecision decision = dataPermissionService.decide(principal);
        long globalPending = (decision.getScope() == DataPermissionScope.ALL && !decision.hasApprovalTypeRestriction())
                ? taskRepository.countByStatusAndDeletedFalse("pending") : 0;
        OffsetDateTime start = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        long todaySubmitted = requestRepository.count((root, query, cb) -> cb.and(
                cb.isFalse(root.get("deleted")),
                cb.greaterThanOrEqualTo(root.get("createdAt"), start)));
        return new DashboardSummaryResponse(myTodo, myInProgress, unread, globalPending, todaySubmitted);
    }

    @Transactional(readOnly = true)
    public StatisticsResponse statistics(UserPrincipal principal) {
        Specification<ApprovalRequest> baseSpec = buildSpec(new ApprovalManagementQuery(), principal);
        long pending = requestRepository.count(combine(baseSpec, "status", "in_progress"));
        long approved = requestRepository.count(combine(baseSpec, "status", "approved"));
        long rejected = requestRepository.count(combine(baseSpec, "status", "rejected"));
        Map<String, Long> typeCounts = new LinkedHashMap<String, Long>();
        for (String type : new String[]{"leave", "expense", "purchase", "overtime", "business_trip"}) {
            long count = requestRepository.count(combine(baseSpec, "type", type));
            if (count > 0) {
                typeCounts.put(type, count);
            }
        }
        Map<String, Long> statusCounts = new LinkedHashMap<String, Long>();
        for (String status : new String[]{"draft", "in_progress", "approved", "rejected", "withdrawn", "need_more_info", "voided"}) {
            long count = requestRepository.count(combine(baseSpec, "status", status));
            if (count > 0) {
                statusCounts.put(status, count);
            }
        }
        return new StatisticsResponse(typeCounts, statusCounts, pending, approved, rejected, taskRepository.countByOverdueTrueAndDeletedFalse());
    }

    private Specification<ApprovalRequest> combine(Specification<ApprovalRequest> base, String field, Object value) {
        return base.and((root, query, cb) -> cb.equal(root.get(field), value));
    }

    private Specification<ApprovalRequest> buildSpec(ApprovalManagementQuery query, UserPrincipal principal) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(cb.isFalse(root.get("deleted")));
            DataPermissionDecision decision = dataPermissionService.decide(principal);
            if (decision.getScope() == DataPermissionScope.SELF) {
                predicates.add(cb.equal(root.get("applicantId"), decision.getUserId()));
            } else if (decision.getScope() == DataPermissionScope.DEPARTMENT) {
                predicates.add(cb.equal(root.get("departmentId"), decision.getDepartmentId()));
            }
            if (decision.hasApprovalTypeRestriction()) {
                predicates.add(root.get("type").in(decision.getAllowedApprovalTypes()));
            }
            if (StringUtils.hasText(query.getType())) {
                if (decision.hasApprovalTypeRestriction()
                        && !decision.getAllowedApprovalTypes().contains(query.getType())) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(cb.equal(root.get("type"), query.getType()));
                }
            }
            if (StringUtils.hasText(query.getStatus())) {
                predicates.add(cb.equal(root.get("status"), query.getStatus()));
            }
            if (StringUtils.hasText(query.getApplicantKeyword())) {
                predicates.add(cb.like(cb.lower(root.get("applicantName")), "%" + query.getApplicantKeyword().toLowerCase() + "%"));
            }
            if (query.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("departmentId"), query.getDepartmentId()));
            }
            if (query.getStartCreatedAt() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), query.getStartCreatedAt()));
            }
            if (query.getEndCreatedAt() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), query.getEndCreatedAt()));
            }
            if (query.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), query.getMinAmount()));
            }
            if (query.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), query.getMaxAmount()));
            }
            if (query.getUrgent() != null) {
                predicates.add(cb.equal(root.get("urgent"), query.getUrgent()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
