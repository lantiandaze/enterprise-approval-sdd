package com.company.approval.approval.repository;

import java.util.List;

import com.company.approval.approval.domain.ApprovalWorkflowNodeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalWorkflowNodeTemplateRepository extends JpaRepository<ApprovalWorkflowNodeTemplate, Long> {

    List<ApprovalWorkflowNodeTemplate> findByTemplateIdAndDeletedFalseOrderBySortOrderAsc(Long templateId);
}
