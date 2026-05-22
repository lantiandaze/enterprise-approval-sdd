package com.company.approval.approval.domain;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "approval_task")
public class ApprovalTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "workflow_instance_id", nullable = false)
    private Long workflowInstanceId;

    @Column(name = "node_instance_id", nullable = false)
    private Long nodeInstanceId;

    @Column(name = "assignee_id", nullable = false)
    private Long assigneeId;

    @Column(name = "assignee_name", nullable = false, length = 100)
    private String assigneeName;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(length = 1000)
    private String comment;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "acted_at")
    private OffsetDateTime actedAt;

    @Column(name = "due_at")
    private OffsetDateTime dueAt;

    @Column(nullable = false)
    private Boolean overdue;

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

    protected ApprovalTask() {
    }

    public ApprovalTask(Long requestId, Long workflowInstanceId, Long nodeInstanceId, Long assigneeId, String assigneeName, Long operatorId) {
        OffsetDateTime now = OffsetDateTime.now();
        this.requestId = requestId;
        this.workflowInstanceId = workflowInstanceId;
        this.nodeInstanceId = nodeInstanceId;
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
        this.status = "pending";
        this.assignedAt = now;
        this.dueAt = now.plusHours(24);
        this.overdue = false;
        this.deleted = false;
        this.createdAt = now;
        this.createdBy = operatorId;
        this.updatedAt = now;
        this.updatedBy = operatorId;
    }

    public void complete(String comment, Long operatorId) {
        finish("completed", comment, operatorId);
    }

    public void finish(String status, String comment, Long operatorId) {
        this.status = status;
        this.comment = comment;
        this.actedAt = OffsetDateTime.now();
        this.updatedAt = this.actedAt;
        this.updatedBy = operatorId;
    }

    public void reassign(Long assigneeId, String assigneeName, String comment, Long operatorId) {
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
        this.comment = comment;
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public void markOverdue(Long operatorId) {
        this.overdue = true;
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public Long getId() { return id; }
    public Long getRequestId() { return requestId; }
    public Long getWorkflowInstanceId() { return workflowInstanceId; }
    public Long getNodeInstanceId() { return nodeInstanceId; }
    public Long getAssigneeId() { return assigneeId; }
    public String getAssigneeName() { return assigneeName; }
    public String getStatus() { return status; }
    public String getComment() { return comment; }
    public OffsetDateTime getAssignedAt() { return assignedAt; }
    public OffsetDateTime getActedAt() { return actedAt; }
    public OffsetDateTime getDueAt() { return dueAt; }
    public Boolean getOverdue() { return overdue; }
}
