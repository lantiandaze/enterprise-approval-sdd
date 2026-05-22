package com.company.approval.approval.dto;

import java.time.OffsetDateTime;

import com.company.approval.approval.domain.ApprovalActionLog;

public class ApprovalActionLogResponse {

    private Long id;
    private String actorName;
    private String action;
    private String fromStatus;
    private String toStatus;
    private String comment;
    private OffsetDateTime createdAt;

    public ApprovalActionLogResponse(ApprovalActionLog log) {
        this.id = log.getId();
        this.actorName = log.getActorName();
        this.action = log.getAction();
        this.fromStatus = log.getFromStatus();
        this.toStatus = log.getToStatus();
        this.comment = log.getComment();
        this.createdAt = log.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getActorName() { return actorName; }
    public String getAction() { return action; }
    public String getFromStatus() { return fromStatus; }
    public String getToStatus() { return toStatus; }
    public String getComment() { return comment; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
