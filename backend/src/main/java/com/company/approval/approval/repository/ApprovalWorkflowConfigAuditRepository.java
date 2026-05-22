package com.company.approval.approval.repository;

import java.util.List;

import com.company.approval.approval.domain.ApprovalWorkflowConfigAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalWorkflowConfigAuditRepository extends JpaRepository<ApprovalWorkflowConfigAudit, Long> {

    List<ApprovalWorkflowConfigAudit> findByDeletedFalseOrderByCreatedAtDesc();
}
