package com.company.approval.approval.dto;

import java.time.OffsetDateTime;

import com.company.approval.approval.domain.ApprovalAttachment;

public class ApprovalAttachmentResponse {

    private Long id;
    private Long requestId;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private OffsetDateTime createdAt;

    public ApprovalAttachmentResponse(ApprovalAttachment attachment) {
        this.id = attachment.getId();
        this.requestId = attachment.getRequestId();
        this.fileName = attachment.getFileName();
        this.contentType = attachment.getContentType();
        this.fileSize = attachment.getFileSize();
        this.createdAt = attachment.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getRequestId() { return requestId; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public Long getFileSize() { return fileSize; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
