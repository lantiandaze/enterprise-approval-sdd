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
@Table(name = "approval_workflow_node_template")
public class ApprovalWorkflowNodeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "node_name", nullable = false, length = 100)
    private String nodeName;

    @Column(name = "approver_rule", nullable = false, length = 64)
    private String approverRule;

    @Column(name = "approver_user_id")
    private Long approverUserId;

    @Column(name = "amount_greater_than")
    private BigDecimal amountGreaterThan;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    protected ApprovalWorkflowNodeTemplate() {
    }

    public ApprovalWorkflowNodeTemplate(Long templateId, String nodeName, String approverRule, Long approverUserId, BigDecimal amountGreaterThan, Integer sortOrder, Long operatorId) {
        OffsetDateTime now = OffsetDateTime.now();
        this.templateId = templateId;
        this.nodeName = nodeName;
        this.approverRule = approverRule;
        this.approverUserId = approverUserId;
        this.amountGreaterThan = amountGreaterThan;
        this.sortOrder = sortOrder;
        this.enabled = true;
        this.deleted = false;
        this.createdAt = now;
        this.createdBy = operatorId;
        this.updatedAt = now;
        this.updatedBy = operatorId;
    }

    public void softDelete(Long operatorId) {
        this.deleted = true;
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = operatorId;
    }

    public Long getId() { return id; }
    public Long getTemplateId() { return templateId; }
    public String getNodeName() { return nodeName; }
    public String getApproverRule() { return approverRule; }
    public Long getApproverUserId() { return approverUserId; }
    public BigDecimal getAmountGreaterThan() { return amountGreaterThan; }
    public Integer getSortOrder() { return sortOrder; }
    public Boolean getEnabled() { return enabled; }
}
