package com.company.approval.approval.repository;

import java.util.List;

import com.company.approval.approval.domain.ApprovalActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalActionLogRepository extends JpaRepository<ApprovalActionLog, Long> {

    List<ApprovalActionLog> findByRequestIdAndDeletedFalseOrderByCreatedAtAsc(Long requestId);

    List<ApprovalActionLog> findByDeletedFalseOrderByCreatedAtDesc();
}
