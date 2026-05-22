package com.company.approval.approval.dto;

import java.time.OffsetDateTime;

import com.company.approval.approval.domain.ApprovalTask;
import com.company.approval.approval.domain.ApprovalWorkflowNodeInstance;

public class ApprovalTimelineNodeResponse {

    private Long id;
    private String nodeName;
    private String approverName;
    private String status;
    private Integer sortOrder;
    private String taskStatus;
    private String comment;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private OffsetDateTime assignedAt;
    private OffsetDateTime actedAt;

    public ApprovalTimelineNodeResponse(ApprovalWorkflowNodeInstance node, ApprovalTask task) {
        this.id = node.getId();
        this.nodeName = node.getNodeName();
        this.approverName = node.getApproverName();
        this.status = node.getStatus();
        this.sortOrder = node.getSortOrder();
        this.startedAt = node.getStartedAt();
        this.completedAt = node.getCompletedAt();
        if (task != null) {
            this.taskStatus = task.getStatus();
            this.comment = task.getComment();
            this.assignedAt = task.getAssignedAt();
            this.actedAt = task.getActedAt();
        }
    }

    public Long getId() { return id; }
    public String getNodeName() { return nodeName; }
    public String getApproverName() { return approverName; }
    public String getStatus() { return status; }
    public Integer getSortOrder() { return sortOrder; }
    public String getTaskStatus() { return taskStatus; }
    public String getComment() { return comment; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public OffsetDateTime getAssignedAt() { return assignedAt; }
    public OffsetDateTime getActedAt() { return actedAt; }
}
