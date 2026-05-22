package com.company.approval.approval.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class ApprovalManagementQuery {

    private String type;
    private String status;
    private String applicantKeyword;
    private Long departmentId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime startCreatedAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime endCreatedAt;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Boolean urgent;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getApplicantKeyword() { return applicantKeyword; }
    public void setApplicantKeyword(String applicantKeyword) { this.applicantKeyword = applicantKeyword; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public OffsetDateTime getStartCreatedAt() { return startCreatedAt; }
    public void setStartCreatedAt(OffsetDateTime startCreatedAt) { this.startCreatedAt = startCreatedAt; }
    public OffsetDateTime getEndCreatedAt() { return endCreatedAt; }
    public void setEndCreatedAt(OffsetDateTime endCreatedAt) { this.endCreatedAt = endCreatedAt; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    public Boolean getUrgent() { return urgent; }
    public void setUrgent(Boolean urgent) { this.urgent = urgent; }
}
