package com.company.approval.approval.repository;

import java.util.List;

import com.company.approval.approval.domain.ApprovalCc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalCcRepository extends JpaRepository<ApprovalCc, Long> {

    List<ApprovalCc> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    boolean existsByRequestIdAndUserIdAndDeletedFalse(Long requestId, Long userId);
}
