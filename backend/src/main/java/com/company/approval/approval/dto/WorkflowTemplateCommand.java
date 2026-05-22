package com.company.approval.approval.dto;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

public class WorkflowTemplateCommand {

    @NotBlank
    private String approvalType;

    @NotBlank
    private String name;

    private Boolean enabled;

    @Valid
    @NotEmpty
    private List<WorkflowNodeTemplateCommand> nodes;

    public String getApprovalType() { return approvalType; }
    public String getName() { return name; }
    public Boolean getEnabled() { return enabled; }
    public List<WorkflowNodeTemplateCommand> getNodes() { return nodes; }
}
