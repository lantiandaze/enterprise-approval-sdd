package com.company.approval.approval.repository;

import java.util.List;
import java.util.Optional;
import java.time.OffsetDateTime;

import com.company.approval.approval.domain.ApprovalTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalTaskRepository extends JpaRepository<ApprovalTask, Long> {

    List<ApprovalTask> findByAssigneeIdAndStatusAndDeletedFalseOrderByAssignedAtDesc(Long assigneeId, String status);

    List<ApprovalTask> findByAssigneeIdAndStatusNotAndDeletedFalseOrderByActedAtDesc(Long assigneeId, String status);

    List<ApprovalTask> findByRequestIdAndStatusAndDeletedFalse(Long requestId, String status);

    List<ApprovalTask> findByStatusAndOverdueFalseAndDueAtBeforeAndDeletedFalse(String status, OffsetDateTime dueAt);

    long countByAssigneeIdAndStatusAndDeletedFalse(Long assigneeId, String status);

    long countByStatusAndDeletedFalse(String status);

    long countByOverdueTrueAndDeletedFalse();

    Optional<ApprovalTask> findByIdAndDeletedFalse(Long id);

    Optional<ApprovalTask> findFirstByRequestIdAndStatusAndDeletedFalseOrderByAssignedAtDesc(Long requestId, String status);

    Optional<ApprovalTask> findFirstByNodeInstanceIdAndDeletedFalseOrderByAssignedAtDesc(Long nodeInstanceId);

    boolean existsByRequestIdAndAssigneeIdAndDeletedFalse(Long requestId, Long assigneeId);
}
