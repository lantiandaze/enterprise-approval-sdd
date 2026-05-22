package com.company.approval.approval.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "approval_request")
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_no", nullable = false, length = 64)
    private String requestNo;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "applicant_name", nullable = false, length = 100)
    private String applicantName;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    @Column(nullable = false)
    private Boolean urgent;

    private BigDecimal amount;

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "form_data", nullable = false)
    private String formData;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    protected ApprovalRequest() {
    }

    public ApprovalRequest(String requestNo, Long applicantId, String applicantName, Long departmentId, String departmentName) {
        OffsetDateTime now = OffsetDateTime.now();
        this.requestNo = requestNo;
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.status = "draft";
        this.urgent = false;
        this.deleted = false;
        this.createdAt = now;
        this.createdBy = applicantId;
        this.updatedAt = now;
        this.updatedBy = applicantId;
    }

    public void updateDraft(String title, String type, Boolean urgent, BigDecimal amount, OffsetDateTime startTime, OffsetDateTime endTime, String formData, Long operatorId) {
        this.title = title;
        this.type = type;
        this.urgent = urgent == null ? false : urgent;
        this.amount = amount;
        this.startTime = startTime;
        this.endTime = endTime;
        this.formData = formData;
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public void submit(Long operatorId) {
        this.status = "in_progress";
        this.submittedAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public void approve(Long operatorId) {
        this.status = "approved";
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public void reject(Long operatorId) {
        this.status = "rejected";
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public void withdraw(Long operatorId) {
        this.status = "withdrawn";
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public void requireMoreInfo(Long operatorId) {
        this.status = "need_more_info";
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public void resubmit(Long operatorId) {
        this.status = "in_progress";
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public void voidRequest(Long operatorId) {
        this.status = "voided";
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
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
    public OffsetDateTime getStartTime() { return startTime; }
    public OffsetDateTime getEndTime() { return endTime; }
    public String getFormData() { return formData; }
    public OffsetDateTime getSubmittedAt() { return submittedAt; }
    public Boolean getDeleted() { return deleted; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
