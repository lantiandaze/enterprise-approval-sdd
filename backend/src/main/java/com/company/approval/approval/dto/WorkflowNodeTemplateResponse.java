package com.company.approval.approval.dto;

import java.math.BigDecimal;

import com.company.approval.approval.domain.ApprovalWorkflowNodeTemplate;

public class WorkflowNodeTemplateResponse {

    private Long id;
    private String nodeName;
    private String approverRule;
    private Long approverUserId;
    private BigDecimal amountGreaterThan;
    private Integer sortOrder;

    public WorkflowNodeTemplateResponse(ApprovalWorkflowNodeTemplate node) {
        this.id = node.getId();
        this.nodeName = node.getNodeName();
        this.approverRule = node.getApproverRule();
        this.approverUserId = node.getApproverUserId();
        this.amountGreaterThan = node.getAmountGreaterThan();
        this.sortOrder = node.getSortOrder();
    }

    public Long getId() { return id; }
    public String getNodeName() { return nodeName; }
    public String getApproverRule() { return approverRule; }
    public Long getApproverUserId() { return approverUserId; }
    public BigDecimal getAmountGreaterThan() { return amountGreaterThan; }
    public Integer getSortOrder() { return sortOrder; }
}
