package com.company.approval.approval.domain;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "approval_workflow_node_instance")
public class ApprovalWorkflowNodeInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workflow_instance_id", nullable = false)
    private Long workflowInstanceId;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "node_name", nullable = false, length = 100)
    private String nodeName;

    @Column(name = "approver_rule", nullable = false, length = 64)
    private String approverRule;

    @Column(name = "approver_id", nullable = false)
    private Long approverId;

    @Column(name = "approver_name", nullable = false, length = 100)
    private String approverName;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

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

    protected ApprovalWorkflowNodeInstance() {
    }

    public ApprovalWorkflowNodeInstance(Long workflowInstanceId, Long requestId, String nodeName, String approverRule, Long approverId, String approverName, Integer sortOrder, Long operatorId) {
        OffsetDateTime now = OffsetDateTime.now();
        this.workflowInstanceId = workflowInstanceId;
        this.requestId = requestId;
        this.nodeName = nodeName;
        this.approverRule = approverRule;
        this.approverId = approverId;
        this.approverName = approverName;
        this.status = "pending";
        this.sortOrder = sortOrder;
        this.deleted = false;
        this.createdAt = now;
        this.createdBy = operatorId;
        this.updatedAt = now;
        this.updatedBy = operatorId;
    }

    public void activate(Long operatorId) {
        this.status = "active";
        this.startedAt = OffsetDateTime.now();
        this.updatedAt = this.startedAt;
        this.updatedBy = operatorId;
    }

    public void approve(Long operatorId) {
        this.status = "approved";
        this.completedAt = OffsetDateTime.now();
        this.updatedAt = this.completedAt;
        this.updatedBy = operatorId;
    }

    public void reject(Long operatorId) {
        this.status = "rejected";
        this.completedAt = OffsetDateTime.now();
        this.updatedAt = this.completedAt;
        this.updatedBy = operatorId;
    }

    public void requireMoreInfo(Long operatorId) {
        this.status = "need_more_info";
        this.completedAt = OffsetDateTime.now();
        this.updatedAt = this.completedAt;
        this.updatedBy = operatorId;
    }

    public void reactivate(Long operatorId) {
        this.status = "active";
        this.completedAt = null;
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public void withdraw(Long operatorId) {
        this.status = "withdrawn";
        this.completedAt = OffsetDateTime.now();
        this.updatedAt = this.completedAt;
        this.updatedBy = operatorId;
    }

    public void voidNode(Long operatorId) {
        this.status = "voided";
        this.completedAt = OffsetDateTime.now();
        this.updatedAt = this.completedAt;
        this.updatedBy = operatorId;
    }

    public void transferApprover(Long approverId, String approverName, Long operatorId) {
        this.approverId = approverId;
        this.approverName = approverName;
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public void shiftSortOrder(Integer delta, Long operatorId) {
        this.sortOrder = this.sortOrder + delta;
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public Long getId() { return id; }
    public Long getWorkflowInstanceId() { return workflowInstanceId; }
    public Long getRequestId() { return requestId; }
    public String getNodeName() { return nodeName; }
    public String getApproverRule() { return approverRule; }
    public Long getApproverId() { return approverId; }
    public String getApproverName() { return approverName; }
    public String getStatus() { return status; }
    public Integer getSortOrder() { return sortOrder; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
}
