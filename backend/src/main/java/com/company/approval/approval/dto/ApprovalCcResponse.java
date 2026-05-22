package com.company.approval.approval.dto;

import java.time.OffsetDateTime;

import com.company.approval.approval.domain.ApprovalCc;
import com.company.approval.approval.domain.ApprovalRequest;

public class ApprovalCcResponse {

    private Long id;
    private Long requestId;
    private String requestNo;
    private String title;
    private String type;
    private String requestStatus;
    private String applicantName;
    private String comment;
    private OffsetDateTime readAt;
    private OffsetDateTime createdAt;

    public ApprovalCcResponse(ApprovalCc cc, ApprovalRequest request) {
        this.id = cc.getId();
        this.requestId = cc.getRequestId();
        this.requestNo = request.getRequestNo();
        this.title = request.getTitle();
        this.type = request.getType();
        this.requestStatus = request.getStatus();
        this.applicantName = request.getApplicantName();
        this.comment = cc.getComment();
        this.readAt = cc.getReadAt();
        this.createdAt = cc.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getRequestId() { return requestId; }
    public String getRequestNo() { return requestNo; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getRequestStatus() { return requestStatus; }
    public String getApplicantName() { return applicantName; }
    public String getComment() { return comment; }
    public OffsetDateTime getReadAt() { return readAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
