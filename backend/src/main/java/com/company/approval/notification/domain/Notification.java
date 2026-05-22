package com.company.approval.notification.domain;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String content;

    @Column(name = "related_request_id")
    private Long relatedRequestId;

    @Column(name = "related_task_id")
    private Long relatedTaskId;

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

    protected Notification() {
    }

    public Notification(Long userId, String type, String title, String content, Long relatedRequestId, Long relatedTaskId, Long operatorId) {
        OffsetDateTime now = OffsetDateTime.now();
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.relatedRequestId = relatedRequestId;
        this.relatedTaskId = relatedTaskId;
        this.deleted = false;
        this.createdAt = now;
        this.createdBy = operatorId;
        this.updatedAt = now;
        this.updatedBy = operatorId;
    }

    public void markRead(Long operatorId) {
        if (this.readAt == null) {
            this.readAt = OffsetDateTime.now();
        }
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Long getRelatedRequestId() { return relatedRequestId; }
    public Long getRelatedTaskId() { return relatedTaskId; }
    public OffsetDateTime getReadAt() { return readAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
