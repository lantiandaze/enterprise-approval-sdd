package com.company.approval.approval.repository;

import java.util.List;

import com.company.approval.approval.domain.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long>, JpaSpecificationExecutor<ApprovalRequest> {

    List<ApprovalRequest> findByApplicantIdAndDeletedFalseOrderByCreatedAtDesc(Long applicantId);
}
