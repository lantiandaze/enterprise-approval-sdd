package com.company.approval.approval.repository;

import java.util.List;

import com.company.approval.approval.domain.ApprovalAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalAttachmentRepository extends JpaRepository<ApprovalAttachment, Long> {

    List<ApprovalAttachment> findByRequestIdAndDeletedFalseOrderByCreatedAtAsc(Long requestId);
}
