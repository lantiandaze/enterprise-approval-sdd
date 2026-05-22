package com.company.approval.organization.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PositionRequest {

    @NotNull
    private Long departmentId;

    @NotBlank
    @Size(max = 64)
    private String code;

    @NotBlank
    @Size(max = 100)
    private String name;

    private Integer sortOrder;

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}
