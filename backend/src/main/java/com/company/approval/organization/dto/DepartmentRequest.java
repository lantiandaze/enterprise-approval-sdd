package com.company.approval.organization.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class DepartmentRequest {

    @NotBlank
    @Size(max = 64)
    private String code;

    @NotBlank
    @Size(max = 100)
    private String name;

    private Long parentId;
    private Long leaderUserId;
    private Integer sortOrder;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Long getParentId() {
        return parentId;
    }

    public Long getLeaderUserId() {
        return leaderUserId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}
