package com.company.approval.approval.domain;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "approval_workflow_config_audit")
public class ApprovalWorkflowConfigAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "actor_name", nullable = false, length = 100)
    private String actorName;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(length = 1000)
    private String detail;

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

    protected ApprovalWorkflowConfigAudit() {
    }

    public ApprovalWorkflowConfigAudit(Long templateId, Long actorId, String actorName, String action, String detail) {
        OffsetDateTime now = OffsetDateTime.now();
        this.templateId = templateId;
        this.actorId = actorId;
        this.actorName = actorName;
        this.action = action;
        this.detail = detail;
        this.deleted = false;
        this.createdAt = now;
        this.createdBy = actorId;
        this.updatedAt = now;
        this.updatedBy = actorId;
    }

    public Long getId() { return id; }
    public Long getTemplateId() { return templateId; }
    public String getActorName() { return actorName; }
    public String getAction() { return action; }
    public String getDetail() { return detail; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
