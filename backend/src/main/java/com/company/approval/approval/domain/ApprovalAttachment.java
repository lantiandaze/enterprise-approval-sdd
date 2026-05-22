package com.company.approval.approval.domain;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "approval_attachment")
public class ApprovalAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "content_type", length = 128)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    protected ApprovalAttachment() {
    }

    public ApprovalAttachment(Long requestId, String fileName, String storagePath, String contentType, Long fileSize, Long operatorId) {
        OffsetDateTime now = OffsetDateTime.now();
        this.requestId = requestId;
        this.fileName = fileName;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.deleted = false;
        this.createdAt = now;
        this.createdBy = operatorId;
        this.updatedAt = now;
        this.updatedBy = operatorId;
    }

    public void softDelete(Long operatorId) {
        this.deleted = true;
        this.deletedAt = OffsetDateTime.now();
        this.deletedBy = operatorId;
        this.updatedAt = this.deletedAt;
        this.updatedBy = operatorId;
    }

    public Long getId() { return id; }
    public Long getRequestId() { return requestId; }
    public String getFileName() { return fileName; }
    public String getStoragePath() { return storagePath; }
    public String getContentType() { return contentType; }
    public Long getFileSize() { return fileSize; }
    public Boolean getDeleted() { return deleted; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
