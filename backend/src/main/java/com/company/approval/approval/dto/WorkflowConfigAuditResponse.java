package com.company.approval.approval.dto;

import java.time.OffsetDateTime;

import com.company.approval.approval.domain.ApprovalWorkflowConfigAudit;

public class WorkflowConfigAuditResponse {

    private Long id;
    private Long templateId;
    private String actorName;
    private String action;
    private String detail;
    private OffsetDateTime createdAt;

    public WorkflowConfigAuditResponse(ApprovalWorkflowConfigAudit audit) {
        this.id = audit.getId();
        this.templateId = audit.getTemplateId();
        this.actorName = audit.getActorName();
        this.action = audit.getAction();
        this.detail = audit.getDetail();
        this.createdAt = audit.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getTemplateId() { return templateId; }
    public String getActorName() { return actorName; }
    public String getAction() { return action; }
    public String getDetail() { return detail; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
