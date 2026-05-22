package com.company.approval.approval.repository;

import java.util.List;
import java.util.Optional;

import com.company.approval.approval.domain.ApprovalWorkflowTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalWorkflowTemplateRepository extends JpaRepository<ApprovalWorkflowTemplate, Long> {

    List<ApprovalWorkflowTemplate> findByDeletedFalseOrderByApprovalTypeAscIdAsc();

    Optional<ApprovalWorkflowTemplate> findFirstByApprovalTypeAndEnabledTrueAndDeletedFalseOrderByUpdatedAtDesc(String approvalType);
}
