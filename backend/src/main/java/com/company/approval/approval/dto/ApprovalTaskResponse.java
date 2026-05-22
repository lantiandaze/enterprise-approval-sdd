package com.company.approval.approval.dto;

import java.time.OffsetDateTime;

import com.company.approval.approval.domain.ApprovalRequest;
import com.company.approval.approval.domain.ApprovalTask;
import com.company.approval.approval.domain.ApprovalWorkflowNodeInstance;

public class ApprovalTaskResponse {

    private Long id;
    private String status;
    private Long requestId;
    private String requestNo;
    private String title;
    private String type;
    private String requestStatus;
    private String applicantName;
    private String departmentName;
    private Long assigneeId;
    private String assigneeName;
    private String nodeName;
    private String comment;
    private OffsetDateTime assignedAt;
    private OffsetDateTime actedAt;
    private OffsetDateTime dueAt;

    public ApprovalTaskResponse(ApprovalTask task, ApprovalRequest request, ApprovalWorkflowNodeInstance node) {
        this.id = task.getId();
        this.status = task.getStatus();
        this.requestId = request.getId();
        this.requestNo = request.getRequestNo();
        this.title = request.getTitle();
        this.type = request.getType();
        this.requestStatus = request.getStatus();
        this.applicantName = request.getApplicantName();
        this.departmentName = request.getDepartmentName();
        this.assigneeId = task.getAssigneeId();
        this.assigneeName = task.getAssigneeName();
        this.nodeName = node == null ? null : node.getNodeName();
        this.comment = task.getComment();
        this.assignedAt = task.getAssignedAt();
        this.actedAt = task.getActedAt();
        this.dueAt = task.getDueAt();
    }

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public Long getRequestId() { return requestId; }
    public String getRequestNo() { return requestNo; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getRequestStatus() { return requestStatus; }
    public String getApplicantName() { return applicantName; }
    public String getDepartmentName() { return departmentName; }
    public Long getAssigneeId() { return assigneeId; }
    public String getAssigneeName() { return assigneeName; }
    public String getNodeName() { return nodeName; }
    public String getComment() { return comment; }
    public OffsetDateTime getAssignedAt() { return assignedAt; }
    public OffsetDateTime getActedAt() { return actedAt; }
    public OffsetDateTime getDueAt() { return dueAt; }
}
