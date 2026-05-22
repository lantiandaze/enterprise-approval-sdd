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
import com.company.approval.notification.repository.NotificationRepository;
import com.company.approval.security.permission.DataPermissionDecision;
import com.company.approval.security.permission.DataPermissionScope;
import com.company.approval.security.permission.DataPermissionService;
import com.company.approval.security.principal.UserPrincipal;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ApprovalManagementService {

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
    public List<ApprovalManagementResponse> list(ApprovalManagementQuery query, UserPrincipal principal) {
        List<ApprovalManagementResponse> responses = new ArrayList<ApprovalManagementResponse>();
        for (ApprovalRequest request : findRequests(query, principal)) {
            responses.add(new ApprovalManagementResponse(request));
        }
        return responses;
    }

    @Transactional
    public byte[] exportCsv(ApprovalManagementQuery query, UserPrincipal principal) {
        if (!principal.getRoles().contains("admin") && !principal.getRoles().contains("general_manager")) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        List<ApprovalRequest> requests = findRequests(query, principal);
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
        if (!requests.isEmpty()) {
            ApprovalRequest first = requests.get(0);
            actionLogRepository.save(new ApprovalActionLog(first.getId(), principal.getUserId(), principal.getDisplayName(), "export", first.getStatus(), first.getStatus(), "导出审批记录：" + requests.size() + " 条"));
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
        long globalPending = dataPermissionService.decide(principal).getScope() == DataPermissionScope.ALL
                ? taskRepository.countByStatusAndDeletedFalse("pending") : 0;
        OffsetDateTime start = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        long todaySubmitted = requestRepository.count((root, query, cb) -> cb.and(
                cb.isFalse(root.get("deleted")),
                cb.greaterThanOrEqualTo(root.get("createdAt"), start)));
        return new DashboardSummaryResponse(myTodo, myInProgress, unread, globalPending, todaySubmitted);
    }

    @Transactional(readOnly = true)
    public StatisticsResponse statistics(UserPrincipal principal) {
        List<ApprovalRequest> requests = findRequests(new ApprovalManagementQuery(), principal);
        Map<String, Long> typeCounts = new LinkedHashMap<String, Long>();
        Map<String, Long> statusCounts = new LinkedHashMap<String, Long>();
        for (ApprovalRequest request : requests) {
            increment(typeCounts, request.getType());
            increment(statusCounts, request.getStatus());
        }
        long pending = statusCounts.containsKey("in_progress") ? statusCounts.get("in_progress") : 0L;
        long approved = statusCounts.containsKey("approved") ? statusCounts.get("approved") : 0L;
        long rejected = statusCounts.containsKey("rejected") ? statusCounts.get("rejected") : 0L;
        return new StatisticsResponse(typeCounts, statusCounts, pending, approved, rejected, taskRepository.countByOverdueTrueAndDeletedFalse());
    }

    private List<ApprovalRequest> findRequests(ApprovalManagementQuery query, UserPrincipal principal) {
        return requestRepository.findAll(buildSpec(query, principal), Sort.by(Sort.Direction.DESC, "createdAt"));
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
            if (StringUtils.hasText(query.getType())) {
                predicates.add(cb.equal(root.get("type"), query.getType()));
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

    private void increment(Map<String, Long> counts, String key) {
        counts.put(key, counts.containsKey(key) ? counts.get(key) + 1 : 1L);
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
