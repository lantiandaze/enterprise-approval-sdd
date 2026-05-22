package com.company.approval.approval.domain;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "approval_cc")
public class ApprovalCc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(length = 1000)
    private String comment;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    protected ApprovalCc() {
    }

    public ApprovalCc(Long requestId, Long userId, String userName, String comment, Long operatorId) {
        OffsetDateTime now = OffsetDateTime.now();
        this.requestId = requestId;
        this.userId = userId;
        this.userName = userName;
        this.comment = comment;
        this.deleted = false;
        this.createdAt = now;
        this.createdBy = operatorId;
        this.updatedAt = now;
        this.updatedBy = operatorId;
    }

    public void markRead(Long operatorId) {
        this.readAt = OffsetDateTime.now();
        this.updatedAt = this.readAt;
        this.updatedBy = operatorId;
    }

    public Long getId() { return id; }
    public Long getRequestId() { return requestId; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getComment() { return comment; }
    public OffsetDateTime getReadAt() { return readAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
