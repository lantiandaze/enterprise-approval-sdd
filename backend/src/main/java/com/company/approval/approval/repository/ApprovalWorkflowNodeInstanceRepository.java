package com.company.approval.approval.repository;

import java.util.List;
import java.util.Optional;

import com.company.approval.approval.domain.ApprovalWorkflowNodeInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalWorkflowNodeInstanceRepository extends JpaRepository<ApprovalWorkflowNodeInstance, Long> {

    List<ApprovalWorkflowNodeInstance> findByRequestIdAndDeletedFalseOrderBySortOrderAsc(Long requestId);

    List<ApprovalWorkflowNodeInstance> findByWorkflowInstanceIdAndDeletedFalseOrderBySortOrderAsc(Long workflowInstanceId);

    Optional<ApprovalWorkflowNodeInstance> findFirstByWorkflowInstanceIdAndStatusAndDeletedFalseOrderBySortOrderAsc(Long workflowInstanceId, String status);

    Optional<ApprovalWorkflowNodeInstance> findFirstByWorkflowInstanceIdAndSortOrderGreaterThanAndDeletedFalseOrderBySortOrderAsc(Long workflowInstanceId, Integer sortOrder);
}
