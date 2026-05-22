package com.company.approval.approval.repository;

import java.util.Optional;

import com.company.approval.approval.domain.ApprovalWorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalWorkflowInstanceRepository extends JpaRepository<ApprovalWorkflowInstance, Long> {

    Optional<ApprovalWorkflowInstance> findByRequestIdAndDeletedFalse(Long requestId);
}
