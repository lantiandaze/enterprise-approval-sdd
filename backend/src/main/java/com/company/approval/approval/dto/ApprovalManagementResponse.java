package com.company.approval.approval.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.company.approval.approval.domain.ApprovalRequest;

public class ApprovalManagementResponse {

    private Long id;
    private String requestNo;
    private String title;
    private String type;
    private String status;
    private Long applicantId;
    private String applicantName;
    private Long departmentId;
    private String departmentName;
    private Boolean urgent;
    private BigDecimal amount;
    private OffsetDateTime submittedAt;
    private OffsetDateTime createdAt;

    public ApprovalManagementResponse(ApprovalRequest request) {
        this.id = request.getId();
        this.requestNo = request.getRequestNo();
        this.title = request.getTitle();
        this.type = request.getType();
        this.status = request.getStatus();
        this.applicantId = request.getApplicantId();
        this.applicantName = request.getApplicantName();
        this.departmentId = request.getDepartmentId();
        this.departmentName = request.getDepartmentName();
        this.urgent = request.getUrgent();
        this.amount = request.getAmount();
        this.submittedAt = request.getSubmittedAt();
        this.createdAt = request.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getRequestNo() { return requestNo; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public Long getApplicantId() { return applicantId; }
    public String getApplicantName() { return applicantName; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public Boolean getUrgent() { return urgent; }
    public BigDecimal getAmount() { return amount; }
    public OffsetDateTime getSubmittedAt() { return submittedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
