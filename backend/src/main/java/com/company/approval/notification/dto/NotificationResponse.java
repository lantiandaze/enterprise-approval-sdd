package com.company.approval.notification.dto;

import java.time.OffsetDateTime;

import com.company.approval.notification.domain.Notification;

public class NotificationResponse {

    private Long id;
    private String type;
    private String title;
    private String content;
    private Long relatedRequestId;
    private Long relatedTaskId;
    private Boolean read;
    private OffsetDateTime readAt;
    private OffsetDateTime createdAt;

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.type = notification.getType();
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.relatedRequestId = notification.getRelatedRequestId();
        this.relatedTaskId = notification.getRelatedTaskId();
        this.readAt = notification.getReadAt();
        this.read = notification.getReadAt() != null;
        this.createdAt = notification.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Long getRelatedRequestId() { return relatedRequestId; }
    public Long getRelatedTaskId() { return relatedTaskId; }
    public Boolean getRead() { return read; }
    public OffsetDateTime getReadAt() { return readAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
