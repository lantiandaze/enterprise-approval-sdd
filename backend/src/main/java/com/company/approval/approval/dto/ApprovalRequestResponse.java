package com.company.approval.approval.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.company.approval.approval.domain.ApprovalRequest;

public class ApprovalRequestResponse {

    private Long id;
    private String requestNo;
    private String title;
    private String type;
    private String status;
    private String applicantName;
    private String departmentName;
    private Boolean urgent;
    private BigDecimal amount;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Map<String, Object> formData;
    private OffsetDateTime submittedAt;
    private OffsetDateTime createdAt;
    private List<ApprovalActionLogResponse> actionLogs;
    private List<ApprovalAttachmentResponse> attachments;
    private List<ApprovalTimelineNodeResponse> timelineNodes;

    public ApprovalRequestResponse(
            ApprovalRequest request,
            Map<String, Object> formData,
            List<ApprovalActionLogResponse> actionLogs,
            List<ApprovalAttachmentResponse> attachments,
            List<ApprovalTimelineNodeResponse> timelineNodes) {
        this.id = request.getId();
        this.requestNo = request.getRequestNo();
        this.title = request.getTitle();
        this.type = request.getType();
        this.status = request.getStatus();
        this.applicantName = request.getApplicantName();
        this.departmentName = request.getDepartmentName();
        this.urgent = request.getUrgent();
        this.amount = request.getAmount();
        this.startTime = request.getStartTime();
        this.endTime = request.getEndTime();
        this.formData = formData;
        this.submittedAt = request.getSubmittedAt();
        this.createdAt = request.getCreatedAt();
        this.actionLogs = actionLogs;
        this.attachments = attachments;
        this.timelineNodes = timelineNodes;
    }

    public Long getId() { return id; }
    public String getRequestNo() { return requestNo; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getApplicantName() { return applicantName; }
    public String getDepartmentName() { return departmentName; }
    public Boolean getUrgent() { return urgent; }
    public BigDecimal getAmount() { return amount; }
    public OffsetDateTime getStartTime() { return startTime; }
    public OffsetDateTime getEndTime() { return endTime; }
    public Map<String, Object> getFormData() { return formData; }
    public OffsetDateTime getSubmittedAt() { return submittedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public List<ApprovalActionLogResponse> getActionLogs() { return actionLogs; }
    public List<ApprovalAttachmentResponse> getAttachments() { return attachments; }
    public List<ApprovalTimelineNodeResponse> getTimelineNodes() { return timelineNodes; }
}
