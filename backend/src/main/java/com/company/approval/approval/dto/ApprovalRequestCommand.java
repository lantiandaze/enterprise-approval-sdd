package com.company.approval.approval.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ApprovalRequestCommand {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String type;

    private Boolean urgent;
    private BigDecimal amount;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    @NotNull
    private Map<String, Object> formData;

    public String getTitle() { return title; }
    public String getType() { return type; }
    public Boolean getUrgent() { return urgent; }
    public BigDecimal getAmount() { return amount; }
    public OffsetDateTime getStartTime() { return startTime; }
    public OffsetDateTime getEndTime() { return endTime; }
    public Map<String, Object> getFormData() { return formData; }
}
