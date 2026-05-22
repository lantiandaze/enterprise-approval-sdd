package com.company.approval.approval.domain;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "approval_workflow_template")
public class ApprovalWorkflowTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "approval_type", nullable = false, length = 32)
    private String approvalType;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Boolean enabled;

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

    protected ApprovalWorkflowTemplate() {
    }

    public ApprovalWorkflowTemplate(String approvalType, String name, Long operatorId) {
        OffsetDateTime now = OffsetDateTime.now();
        this.approvalType = approvalType;
        this.name = name;
        this.enabled = true;
        this.deleted = false;
        this.createdAt = now;
        this.createdBy = operatorId;
        this.updatedAt = now;
        this.updatedBy = operatorId;
    }

    public void update(String approvalType, String name, Boolean enabled, Long operatorId) {
        this.approvalType = approvalType;
        this.name = name;
        this.enabled = enabled == null ? true : enabled;
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public void setEnabled(Boolean enabled, Long operatorId) {
        this.enabled = enabled;
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public Long getId() { return id; }
    public String getApprovalType() { return approvalType; }
    public String getName() { return name; }
    public Boolean getEnabled() { return enabled; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
