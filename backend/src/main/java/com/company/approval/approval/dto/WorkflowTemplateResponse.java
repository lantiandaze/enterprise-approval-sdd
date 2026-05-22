package com.company.approval.approval.dto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.company.approval.approval.domain.ApprovalWorkflowNodeTemplate;
import com.company.approval.approval.domain.ApprovalWorkflowTemplate;

public class WorkflowTemplateResponse {

    private Long id;
    private String approvalType;
    private String name;
    private Boolean enabled;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<WorkflowNodeTemplateResponse> nodes;

    public WorkflowTemplateResponse(ApprovalWorkflowTemplate template, List<ApprovalWorkflowNodeTemplate> nodeTemplates) {
        this.id = template.getId();
        this.approvalType = template.getApprovalType();
        this.name = template.getName();
        this.enabled = template.getEnabled();
        this.createdAt = template.getCreatedAt();
        this.updatedAt = template.getUpdatedAt();
        this.nodes = new ArrayList<WorkflowNodeTemplateResponse>();
        for (ApprovalWorkflowNodeTemplate node : nodeTemplates) {
            this.nodes.add(new WorkflowNodeTemplateResponse(node));
        }
    }

    public Long getId() { return id; }
    public String getApprovalType() { return approvalType; }
    public String getName() { return name; }
    public Boolean getEnabled() { return enabled; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public List<WorkflowNodeTemplateResponse> getNodes() { return nodes; }
}
