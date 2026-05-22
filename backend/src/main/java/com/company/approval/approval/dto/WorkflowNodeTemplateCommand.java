package com.company.approval.approval.dto;

import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class WorkflowNodeTemplateCommand {

    @NotBlank
    private String nodeName;

    @NotBlank
    private String approverRule;

    private Long approverUserId;

    private BigDecimal amountGreaterThan;

    @NotNull
    private Integer sortOrder;

    public String getNodeName() { return nodeName; }
    public String getApproverRule() { return approverRule; }
    public Long getApproverUserId() { return approverUserId; }
    public BigDecimal getAmountGreaterThan() { return amountGreaterThan; }
    public Integer getSortOrder() { return sortOrder; }
}
